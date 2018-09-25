package com.intelligent.reader.activity.usercenter

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.intelligent.reader.R
import com.intelligent.reader.util.TakePictureManager
import com.intelligent.reader.view.BottomDialog
import com.intelligent.reader.view.MenuItem
import com.intelligent.reader.view.login.LoadingDialog
import kotlinx.android.synthetic.qbmfxsydq.act_user_profile.*
import kotlinx.android.synthetic.qbmfxsydq.publish_hint_dialog.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.base.activity.BaseCacheableActivity
import net.lzbook.kit.utils.logi
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.user.Platform
import net.lzbook.kit.utils.user.UserManagerV4
import net.lzbook.kit.widget.MyDialog
import java.io.File

/**
 * Date: 2018/7/27 18:10
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 个人中心
 */
class UserProfileActivity : BaseCacheableActivity() {
    private lateinit var mTakePictureManager: TakePictureManager
    private val genderSelectedColor = Color.parseColor("#1DBFBB")
    private val loadingDialog: LoadingDialog by lazy {
        LoadingDialog(this)
    }

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.act_user_profile)
        initView()
    }

    private fun initView() {
        img_back.setOnClickListener {
            finish()
            val data = java.util.HashMap<String, String>()
            data["type"] = "1"
            StartLogClickUtil.upLoadEventLog(this,
                    StartLogClickUtil.PROFILE, StartLogClickUtil.BACK, data)
        }

        rl_head.setOnClickListener {
            StartLogClickUtil.upLoadEventLog(this,
                    StartLogClickUtil.PROFILE, StartLogClickUtil.PHOTO)
            pictureDialog.show()
        }

        rl_user_name.setOnClickListener {
            loadingDialog.show(getString(R.string.loading_dialog_title_loading))
            UserManagerV4.requestUserNameState { success, result ->
                if (success) {
                    if (result?.data!!.isCanBeModified == 1) {
                        var editIntent = Intent(this, EditUserProfileActivity::class.java)
                        editIntent.putExtra("userName", txt_user_name.text)
                        startActivity(editIntent)
                    } else {
                        ToastUtil.showToastMessage("剩余 ${result!!.data!!.remainingDays} 天可修改昵称")
                    }
                    loadingDialog.dismiss()

                } else {
                    loadingDialog.dismiss()
                    if (result != null) {
                        ToastUtil.showToastMessage(result.message.toString())
                    } else {
                        ToastUtil.showToastMessage(resources.getString(R.string.net_work_error))
                    }

                }


            }
        }

        rl_phone.setOnClickListener {
            val phoneNumber = UserManagerV4.user?.phone_number
            if (phoneNumber == null) {
                val bindingIntent = Intent(this, BindPhoneActivity::class.java)
                startActivity(bindingIntent)
            }
        }


        rl_gender.setOnClickListener {
            StartLogClickUtil.upLoadEventLog(this,
                    StartLogClickUtil.PROFILE, StartLogClickUtil.SEX)
            if (UserManagerV4.user?.gender == getString(R.string.gender_male)) {
                genderDialog.changeItemTextColor(genderSelectedColor, 0)
                genderDialog.changeItemTextColor(Color.BLACK, 1)
            } else if (UserManagerV4.user?.gender == getString(R.string.gender_female)) {
                genderDialog.changeItemTextColor(Color.BLACK, 0)
                genderDialog.changeItemTextColor(genderSelectedColor, 1)
            }
            genderDialog.show()
        }

        rl_platform.setOnClickListener {
            val loginChannel = UserManagerV4.user?.login_channel //第三方登录
            val thirdBindingInfo = UserManagerV4.user?.link_channel //绑定的第三方账户
            if (loginChannel == null && thirdBindingInfo == null) {
                platformDialog.show()
            }
        }


    }


    /**
     * 选头像
     */
    private val pictureDialog: BottomDialog by lazy {
        val menuList = listOf(MenuItem("拍照", Color.BLACK)
                , MenuItem("从相册选择", Color.BLACK))
        val dialog = BottomDialog.newBuilder(this)
                .addMenu(menuList)
                .setOnMenuClickListener { position ->
                    when (position) {
                        1 -> {
                            pictureDialog.dismiss()
                            takePhoto()
                        }
                        2 -> {
                            pictureDialog.dismiss()
                            choicePicture()
                        }
                    }
                }
                .build()
        dialog
    }

    /**
     * 拍照
     */
    private fun takePhoto() {
        mTakePictureManager = TakePictureManager(this)
        //开启裁剪 比例 1:3 宽高 350 350  (默认不裁剪)
        mTakePictureManager.setTailor(1, 1, 200, 200)
        mTakePictureManager.startTakeWayByCarema()
        mTakePictureManager.setTakePictureCallBackListener(object : TakePictureManager.takePictureCallBackListener {
            override fun successful(isTailor: Boolean, outFile: File?, filePath: Uri?) {
                uploadFile(outFile)
            }

            override fun failed(errorCode: Int, deniedPermissions: MutableList<String>?) {
                ToastUtil.showToastMessage("选择图片错误")
                loadingDialog.dismiss()
            }
        })
    }

    /**
     * 选择相册
     */
    private fun choicePicture() {
        mTakePictureManager = TakePictureManager(this)
        //开启裁剪 比例 1:3 宽高 350 350  (默认不裁剪)
        mTakePictureManager.setTailor(1, 1, 200, 200)
        mTakePictureManager.startTakeWayByAlbum()
        mTakePictureManager.setTakePictureCallBackListener(object : TakePictureManager.takePictureCallBackListener {
            override fun successful(isTailor: Boolean, outFile: File?, filePath: Uri?) {
                uploadFile(outFile)
            }

            override fun failed(errorCode: Int, deniedPermissions: MutableList<String>?) {
                ToastUtil.showToastMessage("选择图片错误")
                loadingDialog.dismiss()
            }
        })
    }

    private fun uploadFile(outFile: File?) {
        if (outFile == null) return
        val bitmap = BitmapFactory.decodeFile(outFile.path)
        loadingDialog.show(getString(R.string.uploading))
        UserManagerV4.uploadUserAvatar(bitmap) { success, result ->
            if (success) {
                Glide.with(this@UserProfileActivity).load(outFile).into(img_head)
                UserManagerV4.user?.let {
                    it.avatar_url = result?.data?.avatar_url
                    UserManagerV4.updateUser(it)
                }
                loadingDialog.dismiss()
            } else {
                loadingDialog.dismiss()
                if (result != null) {
                    ToastUtil.showToastMessage(result.message!!)
                } else {
                    ToastUtil.showToastMessage("网络不给力哦，请稍后再试")
                }
            }

        }
//
    }

    /**
     * 选性别
     */
    private val genderDialog: BottomDialog by lazy {
        val menuList = listOf(MenuItem(getString(R.string.gender_male), Color.BLACK)
                , MenuItem(getString(R.string.gender_female), Color.BLACK))
        val dialog = BottomDialog.newBuilder(this)
                .addMenu(menuList)
                .setOnMenuClickListener { position ->
                    genderDialog.dismiss()
                    loadingDialog.show(getString(R.string.loading_dialog_title_editing))
                    val gender = if (position == 1) getString(R.string.gender_male) else getString(R.string.gender_female)
                    UserManagerV4.uploadUserGender(gender) { success, result ->
                        if (success) {
                            UserManagerV4.user?.let {
                                it.gender = result!!.data!!.gender
                                UserManagerV4.updateUser(it)
                            }
                            txt_gender.text = gender
                            loadingDialog.dismiss()

                        } else {
                            loadingDialog.dismiss()
                            if (result != null) {
                                ToastUtil.showToastMessage(result.message.toString())
                            } else {
                                ToastUtil.showToastMessage(resources.getString(R.string.net_work_error))
                            }

                        }
                    }

                }
                .build()
        dialog
    }

    /**
     * 选第三方
     */
    private val platformDialog: BottomDialog by lazy {
        val menuList = listOf(MenuItem("微信", Color.BLACK)
                , MenuItem("QQ", Color.BLACK))
        val dialog = BottomDialog.newBuilder(this)
                .addMenu(menuList)
                .setOnMenuClickListener { position ->
                    when (position) {
                        1 -> {
                            if (!UserManagerV4.isPlatformEnable(Platform.WECHAT)) {
                                ToastUtil.showToastMessage("请安装微信后重试")
                                return@setOnMenuClickListener
                            }
                            showProgressDialog()
                            platformDialog.dismiss()
                            bindingPlatform(Platform.WECHAT)
                        }
                        2 -> {
                            showProgressDialog()
                            platformDialog.dismiss()
                            bindingPlatform(Platform.QQ)
                        }
                    }
                }.build()
        dialog
    }
    /**
     * 进度条
     */
    private val progressDialog: MyDialog by lazy {
        val progressDialog = MyDialog(this, R.layout.publish_hint_dialog)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setCancelable(true)

        progressDialog.publish_content.visibility = View.GONE
        progressDialog.dialog_title.setText(R.string.tips_login)
        progressDialog.change_source_bottom.visibility = View.GONE
        progressDialog.progress_del.visibility = View.VISIBLE
        progressDialog.setOnDismissListener { }
        progressDialog
    }

    private fun showProgressDialog() {
        if (!progressDialog.isShowing) {
            progressDialog.show()
        }
    }

    private fun dismissProgressDialog() {
        progressDialog.dismiss()
        onResume()
    }

    private fun bindingPlatform(platform: Platform) {
        val data = java.util.HashMap<String, String>()
        data["type"] = if (platform == Platform.WECHAT) "1" else "2"
        StartLogClickUtil.upLoadEventLog(this,
                StartLogClickUtil.PROFILE, StartLogClickUtil.BINDOTHERLOGIN, data)
        UserManagerV4.thirdLogin(this, platform, true,
                onSuccess = { it ->
                    ToastUtil.showToastMessage(getString(R.string.bind_success))
                    UserManagerV4.updateUser(it.data!!)
                    dismissProgressDialog()
                },
                onFailure = { t ->
                    ToastUtil.showToastMessage(t)
                    dismissProgressDialog()
                })
    }

    override fun onResume() {
        super.onResume()
        if (!UserManagerV4.isUserLogin) {
            finish()
            return
        }

        val userAvatarUrl = UserManagerV4.user?.avatar_url ?: ""
        if (userAvatarUrl.isNotEmpty()) {
            Glide.with(this)
                    .load(userAvatarUrl)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .placeholder(R.drawable.default_head)
                    .error(R.drawable.default_head)
                    .dontAnimate()
                    .into(img_head)
        } else {
            img_head.setImageResource(R.drawable.default_head)
        }

        txt_user_id.text = UserManagerV4.user?.global_number.toString()

        txt_user_name.text = UserManagerV4.user?.name

        txt_gender.text = UserManagerV4.user?.gender ?: "未知"

        var phoneNumber = UserManagerV4.user?.phone_number
        if (phoneNumber == null) {
            img_phone_arrow.visibility = View.VISIBLE
            txt_phone.text = getString(R.string.bind_phone_title)
        } else {
            phoneNumber = phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7, phoneNumber.length)
            img_phone_arrow.visibility = View.GONE
            txt_phone.text = phoneNumber
        }

        val loginChannel = UserManagerV4.user?.login_channel //第三方登录
                ?.replace("qq", "QQ")
                ?.replace("wechat", "微信")
        val thirdBindingInfo = UserManagerV4.user?.link_channel //绑定的第三方账户
                ?.replace("qq", "QQ")
                ?.replace("wechat", "微信")
        logi("loginChannel: $loginChannel", "thirdBindingInfo: $thirdBindingInfo")
        txt_platform.text = loginChannel ?: thirdBindingInfo ?: getString(R.string.bind_third_account)
    }

    //把本地的onActivityResult()方法回调绑定到对象
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TakePictureManager.CODE_ORIGINAL_PHOTO_CAMERA
                || requestCode == TakePictureManager.CODE_ORIGINAL_PHOTO_ALBUM
                || requestCode == TakePictureManager.CODE_TAILOR_PHOTO) {
            mTakePictureManager.attachToActivityForResult(requestCode, resultCode, data)
        } else {
            UserManagerV4.onActivityResult(requestCode, resultCode, data)
        }
    }

    //onRequestPermissionsResult()方法权限回调绑定到对象
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mTakePictureManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}