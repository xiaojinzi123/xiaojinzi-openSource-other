package com.cxj.imageselect.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.cxj.imageselect.entity.MessageDataHolder;
import com.cxj.imageselect.adapter.ImageAdapter;
import com.cxj.imageselect.localImage.LocalImageInfo;
import com.cxj.imageselect.localImage.LocalImageManager;
import com.cxj.imageselect.popupWindow.ListImageDirPopupWindow;
import com.cxj.imageselect.util.ThreadPool;
import com.example.cxj.imageselect.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import xiaojinzi.EBus.EBus;
import xiaojinzi.base.android.adapter.recyclerView.CommonRecyclerViewAdapter;
import xiaojinzi.base.android.os.ScreenUtils;


/**
 * 选择本地图片的activity
 */
public class SelectLocalImageAct extends Activity implements View.OnClickListener, CommonRecyclerViewAdapter.OnRecyclerViewItemClickListener, Runnable {

    /**
     * 类的标识
     */
    public static final String TAG = "SelectLocalImageAct";

    /**
     * activity带回去的数据的标识
     */
    public static final String RETURN_DATA_FLAG = "data";

    /**
     * 结果码
     */
    public static final int RESULT_CODE = 666;


    private RecyclerView rv = null;

    /**
     * PopupWindow
     */
    private ListImageDirPopupWindow listImageDirPopupWindow;

    /**
     * 返回图标
     */
    private ImageView iv_back;

    /**
     * 确定
     */
    private TextView tv_ok;

    /**
     * 底部的控件
     */
    private LinearLayout ll_info = null;


    /**
     * 显示文件夹名称的控件
     */
    private TextView tv_folderName;


    /**
     * 显示文件夹中图片文件的个数
     */
    private TextView tv_imageNumber;

    /**
     * 适配器
     */
    private CommonRecyclerViewAdapter<String> adapter;

    /**
     * 本地图片的信息
     */
    private LocalImageInfo localImageInfo;

    /**
     * 显示的数据u
     */
    private List<String> data = new ArrayList<String>();

    /**
     * 临时数据
     */
    private List<String> tmpData;

    /**
     * 记录图片是不是被选中,利用下标进行关联
     */
    private List<Boolean> imageStates = new ArrayList<Boolean>();

    /**
     * 上下文
     */
    private Context context;


    private Handler h = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            MessageDataHolder m = (MessageDataHolder) msg.obj;

            //设置底部的信息
            tv_folderName.setText(m.folderName);
            tv_imageNumber.setText(m.imageNum + "张");

            //把临时数据中的数据加入到原来的集合
            data.clear();
            data.addAll(tmpData);
            tmpData = null;

            //初始化选中状态的记录集合
            imageStates.clear();
            for (int i = 0; i < data.size(); i++) {
                imageStates.add(false);
            }

            if (listImageDirPopupWindow != null) {
                listImageDirPopupWindow.dismiss();
                setBackAlpha(false);
            }

            adapter.notifyDataSetChanged();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EBus.register(TAG, this);

        //初始化控件
        initView();

        initEvent();

        rv.setLayoutManager(new GridLayoutManager(this, 3));

        //线程池执行任务
        ThreadPool.getInstance().invoke(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EBus.unRegister(TAG);
    }

    /**
     * 初始化监听事件
     */
    private void initEvent() {

        iv_back.setOnClickListener(this);
        tv_ok.setOnClickListener(this);

        //设置底部菜单的监听
        ll_info.setOnClickListener(this);

        //设置ReCyclerView的条目监听
        adapter.setOnRecyclerViewItemClickListener(this);
    }

    /**
     * 初始化控件
     */
    private void initView() {
        context = this;

        iv_back = (ImageView) findViewById(R.id.iv_act_main_back);
        tv_ok = (TextView) findViewById(R.id.tv_act_main_ok);

        ll_info = (LinearLayout) findViewById(R.id.ll_act_main_info);
        tv_folderName = (TextView) findViewById(R.id.tv_act_main_image_folder_name);
        tv_imageNumber = (TextView) findViewById(R.id.tv_act_main_image_number);


        rv = (RecyclerView) findViewById(R.id.rv);
        //创建适配器
        adapter = new ImageAdapter(context, data, imageStates);
        //设置适配器
        rv.setAdapter(adapter);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ll_act_main_info:
                if (listImageDirPopupWindow != null) {
                    listImageDirPopupWindow
                            .setAnimationStyle(R.style.anim_popup_dir);
                    listImageDirPopupWindow.showAsDropDown(ll_info, 0, 0);
                }
                // 设置背景颜色变暗
                setBackAlpha(true);
                break;
            case R.id.iv_act_main_back:
                finish();
                break;
            case R.id.tv_act_main_ok:
                Intent i = new Intent();
                i.putExtra(RETURN_DATA_FLAG, getSelectImages());
                setResult(RESULT_CODE, i);
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(View v, int position) {
        imageStates.set(position, !imageStates.get(position));
        adapter.notifyDataSetChanged();
    }

    /**
     * 根据文件夹的路径,进行加载图片
     *
     * @param folderPath
     */
    public void onEventLoadImageByFolderPath(final String folderPath) {
        if ("System".equals(folderPath)) {
            //线程池执行任务
            ThreadPool.getInstance().invoke(this);
            return;
        }
        File folder = new File(folderPath);
        //文件夹存在并且是一个目录
        if (folder.exists() && folder.isDirectory()) {
            ThreadPool.getInstance().invoke(new Runnable() {
                @Override
                public void run() {
                    tmpData = LocalImageManager.queryImageByFolderPath(localImageInfo, folderPath);
                    //发送消息
                    h.sendMessage(MessageDataHolder.obtain(folderPath, tmpData.size()));
                }
            });

        }
    }

    @Override
    public void run() {
        //初始化本地图片的管理者
        LocalImageManager.init(context);

        if (localImageInfo == null) {
            //获取本地图片的信息
            localImageInfo = LocalImageManager.
                    queryImageWithFolder(LocalImageManager.PNG_MIME_TYPE, LocalImageManager.JPEG_MIME_TYPE, LocalImageManager.JPG_MIME_TYPE);

        }

        //拿到数据
        tmpData = localImageInfo.getImageFiles();

        if (listImageDirPopupWindow == null) {
            //初始化弹出框
            View contentView = View.inflate(context, R.layout.list_dir, null);
            //创建要弹出的popupWindow
            listImageDirPopupWindow = new ListImageDirPopupWindow(contentView,
                    ScreenUtils.getScreenWidth(context),
                    ScreenUtils.getScreenHeight(context) * 2 / 3,
                    true, localImageInfo);

            //消失的时候监听
            listImageDirPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    setBackAlpha(false);
                }
            });

        }
        //发送消息
        h.sendMessage(MessageDataHolder.obtain("所有文件", tmpData.size()));
    }

    /**
     * 设置窗体的透明度,根据PopupWindow是否打开
     *
     * @param isOpen
     */
    private void setBackAlpha(boolean isOpen) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if (isOpen) {
            lp.alpha = .3f;
        } else {
            lp.alpha = 1.0f;
        }
        getWindow().setAttributes(lp);
    }

    /**
     * 获取被选中的图片的数组
     *
     * @return
     */
    private String[] getSelectImages() {
        List<String> tmp = new ArrayList<String>();
        for (int i = 0; i < imageStates.size(); i++) {
            if (imageStates.get(i)) {
                tmp.add(data.get(i));
            }
        }
        String[] arr = new String[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) {
            arr[i] = tmp.get(i);
        }
        tmp = null;
        return arr;
    }

}
