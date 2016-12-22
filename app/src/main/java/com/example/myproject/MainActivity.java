package com.example.myproject;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import com.example.myproject.DropView.DropLayout;
import com.example.myproject.DropView.SelfSlideView;
import com.example.myproject.DropView.SlideViewGroup;

public class MainActivity extends Activity {
    private DropLayout dropLayout;
    private TextView openDropTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        setStatusBarDisappear();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        dropLayout = (DropLayout) findViewById(R.id.drop_layout);
        openDropTv = (TextView) findViewById(R.id.openDrop);
    }

    private void init() {
        dropLayout.addSlideViewToDropView(R.layout.title_drop_slide_view_group, R.layout.title_drop_slide_view);
        ((SlideViewGroup) dropLayout.getDropView()).setSlideWidth(140);
        dropLayout.setAlphaTime(500);
        dropLayout.setDropTime(500);

        openDropTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dropLayout.startDrop();
                startOpenDropViewAnim();
            }
        });

        SelfSlideView selfSlideView = (SelfSlideView) ((SlideViewGroup) dropLayout.getDropView()).getSlideView();
        final TextView contentView = (TextView) dropLayout.findViewById(R.id.contentTv);
        if (selfSlideView != null) {
            selfSlideView.setOnItemChangdListener(new SelfSlideView.OnItemChangdListener() {
                @Override
                public void onChanged(int nowPosition) {
                    switch (nowPosition){
                        case 0:
                            contentView.setText("凤凰新闻是凤凰新媒体旗下一款资讯类的APP，采用人工智能混合推荐，根据用户兴趣及使用习惯推送的内容，包含由专业内容运营团队编辑推荐内容及凤凰卫视全部节目。");
                            break;
                        case 1:
                            contentView.setText("今日头条是一款基于数据挖掘的推荐引擎产品，它为用户推荐有价值的、个性化的信息，提供连接人与信息的新型服务，是国内移动互联网领域成长最快的产品服务之一。");
                            break;
                        case 2:
                            contentView.setText("速览新闻后续将加入更多新闻平台，为您提供快速方便的咨询。");
                            break;
                    }
                }
            });
        }

    }

    //沉浸式状态栏
    private void setStatusBarDisappear() {
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }

    private boolean isOpenDropViewRotate;

    private void startOpenDropViewAnim() {
        if (!isOpenDropViewRotate) {
            AnimUtil.RotateAnim(openDropTv, 300, 0, 90);
        } else {
            AnimUtil.RotateAnim(openDropTv, 300, 90, 0);
        }
        isOpenDropViewRotate = !isOpenDropViewRotate;
    }
}
