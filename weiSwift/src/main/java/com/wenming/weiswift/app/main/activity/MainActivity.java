package com.wenming.weiswift.app.main.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.wenming.library.LogReport;
import com.wenming.weiswift.R;
import com.wenming.weiswift.app.common.BottomBarManager;
import com.wenming.weiswift.app.common.ApplicationHelper;
import com.wenming.weiswift.app.common.StatusBarUtils;
import com.wenming.weiswift.app.common.base.BaseAppCompatActivity;
import com.wenming.weiswift.app.common.entity.User;
import com.wenming.weiswift.app.common.user.UserManager;
import com.wenming.weiswift.app.debug.DebugTool;
import com.wenming.weiswift.app.discover.DiscoverFragment;
import com.wenming.weiswift.app.home.data.HomeDataManager;
import com.wenming.weiswift.app.home.fragment.HomeFragment;
import com.wenming.weiswift.app.home.presenter.HomePresenter;
import com.wenming.weiswift.app.login.post.PostSwipeActivity;
import com.wenming.weiswift.app.message.fragment.fragment.MessageFragment;
import com.wenming.weiswift.app.myself.fragment.MySelfFragment;
import com.wenming.weiswift.utils.SharedPreferencesUtil;


public class MainActivity extends BaseAppCompatActivity {
    private static final String TAB_HOME_FRAGMENT = "home";
    private static final String TAB_MESSAGE_FRAGMENT = "message";
    private static final String TAB_DISCOVERY_FRAGMENT = "discovery";
    private static final String TAB_PROFILE_FRAGMENT = "profile";

    public static final String EXTRA_REFRESH_ALL = "extra_refresh_all";

    private HomeFragment mHomeFragment;
    private MessageFragment mMessageFragment;
    private DiscoverFragment mDiscoverFragment;
    private MySelfFragment mMySelfFragment;
    private FragmentManager mFragmentManager;
    private RelativeLayout mHomeTabRl, mMessageTabRl, mDiscoverTabRl, mMySelfTabRl;
    private ImageView mPostTabIv;
    private LinearLayout mButtonBarLl;
    private FragmentTransaction mTransaction;

    private String mCurrentIndex;
    private boolean mRefreshAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prepareView();
        initData();
        initView();
        initListener();
//        如果是从崩溃中恢复，还需要加载之前的缓存
        if (savedInstanceState != null) {
            restoreFragment(savedInstanceState);
        } else {
            setTabFragment(TAB_HOME_FRAGMENT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BottomBarManager.getInstance().release();
    }

    private void prepareView() {
        mHomeTabRl = (RelativeLayout) findViewById(R.id.tv_home);
        mMessageTabRl = (RelativeLayout) findViewById(R.id.tv_message);
        mDiscoverTabRl = (RelativeLayout) findViewById(R.id.tv_discovery);
        mMySelfTabRl = (RelativeLayout) findViewById(R.id.tv_profile);
        mPostTabIv = (ImageView) findViewById(R.id.fl_post);
        mButtonBarLl = (LinearLayout) findViewById(R.id.buttonBarId);
    }

    private void initData() {
        mRefreshAll = getIntent().getBooleanExtra(EXTRA_REFRESH_ALL, false);
    }

    private void initView() {
        LogReport.getInstance().upload(mContext);
        DebugTool.showEnvironment(mContext);
        BottomBarManager.getInstance().setBottomView(mButtonBarLl);
        mFragmentManager = getSupportFragmentManager();
        initStatusBar();
    }

    private void initStatusBar() {
        if (!(boolean) SharedPreferencesUtil.get(this, "setNightMode", false)) {
            StatusBarUtils.from(this)
                    .setTransparentStatusbar(true)
                    .setStatusBarColor(getResources().getColor(R.color.home_status_bg))
                    .setLightStatusBar(true)
                    .process(this);
        } else {
            StatusBarUtils.from(this)
                    .setTransparentStatusbar(true)
                    .setStatusBarColor(getResources().getColor(R.color.home_status_bg))
                    .process(this);
        }
    }

    private void initListener() {
        mHomeTabRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTabFragment(TAB_HOME_FRAGMENT);
            }
        });
        mMessageTabRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTabFragment(TAB_MESSAGE_FRAGMENT);
            }
        });
        mPostTabIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PostSwipeActivity.class);
                startActivity(intent);
            }
        });

        mDiscoverTabRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTabFragment(TAB_DISCOVERY_FRAGMENT);
            }
        });

        mMySelfTabRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTabFragment(TAB_PROFILE_FRAGMENT);
            }
        });

    }

    /**
     * 如果fragment因为内存不够或者其他原因被销毁掉，在这个方法中执行恢复操作
     */
    private void restoreFragment(Bundle savedInstanceState) {
        mCurrentIndex = savedInstanceState.getString("index");
        mHomeFragment = (HomeFragment) mFragmentManager.findFragmentByTag(TAB_HOME_FRAGMENT);
        mMessageFragment = (MessageFragment) mFragmentManager.findFragmentByTag(TAB_MESSAGE_FRAGMENT);
        mDiscoverFragment = (DiscoverFragment) mFragmentManager.findFragmentByTag(TAB_DISCOVERY_FRAGMENT);
        mMySelfFragment = (MySelfFragment) mFragmentManager.findFragmentByTag(TAB_PROFILE_FRAGMENT);
        switchToFragment(mCurrentIndex);
    }

    /**
     * Activity被销毁的时候，要记录当前处于哪个页面
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("index", mCurrentIndex);
        super.onSaveInstanceState(outState);
    }

    /**
     * 执行切换fragment 的操作
     * 注意：
     * 1. 切换页面的时候，还要调用showBottomBar来保证底部导航栏的显示
     */
    private void switchToFragment(String index) {
        mButtonBarLl.clearAnimation();
        mTransaction = mFragmentManager.beginTransaction();
        hideAllFragments(mTransaction);
        switch (index) {
            case TAB_HOME_FRAGMENT:
                showHomeFragment();
                break;
            case TAB_MESSAGE_FRAGMENT:
                showMessageFragment();
                break;
            case TAB_DISCOVERY_FRAGMENT:
                showDiscoveryFragment();
                break;
            case TAB_PROFILE_FRAGMENT:
                showProfileFragment();
                break;
        }
        mCurrentIndex = index;
        mTransaction.commit();
    }

    /**
     * 切换到首页模块
     */
    private void showHomeFragment() {
        mHomeTabRl.setSelected(true);
        if (mHomeFragment == null) {
            mHomeFragment = HomeFragment.newInstance(mRefreshAll);
            new HomePresenter(new HomeDataManager(mContext), mHomeFragment);
            mTransaction.add(R.id.main_content_fl, mHomeFragment, TAB_HOME_FRAGMENT);
        } else {
            mTransaction.show(mHomeFragment);
        }
    }

    /**
     * 切换到消息模块
     */
    private void showMessageFragment() {
        mMessageTabRl.setSelected(true);
        if (mMessageFragment == null) {
            mMessageFragment = new MessageFragment();
            mTransaction.add(R.id.main_content_fl, mMessageFragment, TAB_MESSAGE_FRAGMENT);
        } else {
            mTransaction.show(mMessageFragment);
        }
    }

    /**
     * 切换到发现模块
     */
    private void showDiscoveryFragment() {
        mDiscoverTabRl.setSelected(true);
        if (mDiscoverFragment == null) {
            mDiscoverFragment = new DiscoverFragment();
            mTransaction.add(R.id.main_content_fl, mDiscoverFragment, TAB_DISCOVERY_FRAGMENT);
        } else {
            mTransaction.show(mDiscoverFragment);
        }
    }

    /**
     * 切换到关于我模块
     */
    private void showProfileFragment() {
        mMySelfTabRl.setSelected(true);
        if (mMySelfFragment == null) {
            User currentUser = UserManager.getInstance().getUser();
            if (mHomeFragment != null && currentUser != null) {
                mMySelfFragment = MySelfFragment.newInstance(currentUser);
            } else {
                mMySelfFragment = MySelfFragment.newInstance();
            }
            mTransaction.add(R.id.main_content_fl, mMySelfFragment, TAB_PROFILE_FRAGMENT);
        } else {
            mTransaction.show(mMySelfFragment);

        }
    }


    /**
     * 显示指定的fragment，并且把对应的导航栏的icon设置成高亮状态
     * 注意：
     * 1. 如果选项卡已经位于当前页，则执行其他操作
     *
     * @param tabName 需要切换到的具体页面
     */
    private void setTabFragment(String tabName) {
        if (!tabName.equals(mCurrentIndex)) {
            switchToFragment(tabName);
        } else {
            alreadyAtFragment(mCurrentIndex);
        }
    }

    /**
     * 如果选项卡已经位于当前页
     * 1. 对于首页fragment，执行：滑动到顶部，并且刷新时间线，获取最新微博
     * 2. 对于消息fragment，执行：无
     * 3. 对于发现fragment，执行：无
     * 4. 对于关于我fragment，执行：无
     *
     */
    private void alreadyAtFragment(String currentIndex) {
        //如果在当前页
        switch (currentIndex) {
            case TAB_HOME_FRAGMENT:
                if (mHomeFragment != null) {
                    mHomeFragment.scrollToTop();
                }
                break;
            case TAB_MESSAGE_FRAGMENT:
                break;
            case TAB_DISCOVERY_FRAGMENT:
                break;
            case TAB_PROFILE_FRAGMENT:
                break;
        }
    }


    /**
     * 隐藏所有的fragment，并且取消所有的底部导航栏的icon的高亮状态
     */
    private void hideAllFragments(FragmentTransaction transaction) {
        if (mHomeFragment != null) {
            transaction.hide(mHomeFragment);
        }
        if (mMessageFragment != null) {
            transaction.hide(mMessageFragment);
        }

        if (mDiscoverFragment != null) {
            transaction.hide(mDiscoverFragment);
        }
        if (mMySelfFragment != null) {
            transaction.hide(mMySelfFragment);
        }
        mHomeTabRl.setSelected(false);
        mMessageTabRl.setSelected(false);
        mDiscoverTabRl.setSelected(false);
        mMySelfTabRl.setSelected(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mHomeFragment != null) {
            mHomeFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 监听返回按钮
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
            showExitDialog();
        }
        return false;
    }

    /**
     * 显示退出窗口
     */
    public void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("确定要退出？")
                .setCancelable(true)
                .setIcon(R.drawable.logo)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((ApplicationHelper) getApplication()).finishAll();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
