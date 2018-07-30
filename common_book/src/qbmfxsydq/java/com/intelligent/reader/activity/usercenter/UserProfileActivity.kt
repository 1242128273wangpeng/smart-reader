package com.intelligent.reader.activity.usercenter

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import com.bumptech.glide.Glide
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.R
import com.intelligent.reader.util.TakePictureManager
import com.intelligent.reader.view.BottomDialog
import com.intelligent.reader.view.MenuItem
import com.intelligent.reader.view.login.LoadingDialog
import iyouqu.theme.BaseCacheableActivity
import kotlinx.android.synthetic.qbmfxsydq.act_user_profile.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.user.UserManager
import java.io.File

/**
 * Date: 2018/7/27 18:10
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 个人中心
 */
class UserProfileActivity : BaseCacheableActivity() {
    private lateinit var mTakePictureManager: TakePictureManager

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
                showToastMessage("选择图片错误")
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
                showToastMessage("选择图片错误")
                loadingDialog.dismiss()
            }
        })
    }

    private fun uploadFile(outFile: File?) {
        if (outFile == null) return
        val bitmap = BitmapFactory.decodeFile(outFile.path)
        loadingDialog.show(getString(R.string.uploading))
        UserManager.uploadUserAvatar(bitmap) { success, result ->
            if (success) {
                Glide.with(this@UserProfileActivity).load(outFile).into(img_head)
                UserManager.user?.let {
                    it.avatar_url = result?.data?.avatar_url
                    UserManager.updateUser(it)
                }
                loadingDialog.dismiss()
            }else{

            }

        }
//        UserManager.uploadUserAvatar(bitmap, "jpg", {
//            onSuccess { result ->
//                Glide.with(this@UserProfileActivity).load(outFile).into(img_head)
//                UserManager.user?.let {
//                    it.avatarUrl = result.avatarUrl
//                    UserManager.updateUser(it)
//                }
//                loadingDialog.dismiss()
//            }
//            onFailed {
//                loadingDialog.dismiss()
//                loge(it.message.toString())
//                if (it is LoginError) {
//                    toastShort(it.message.toString(), false)
//                } else {
//                    toastShort("网络不给力哦，请稍后再试", false)
//                }
//            }
//        })
    }


    //把本地的onActivityResult()方法回调绑定到对象
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TakePictureManager.CODE_ORIGINAL_PHOTO_CAMERA
                || requestCode == TakePictureManager.CODE_ORIGINAL_PHOTO_ALBUM
                || requestCode == TakePictureManager.CODE_TAILOR_PHOTO) {
            mTakePictureManager.attachToActivityForResult(requestCode, resultCode, data)
        } else {
            UserManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    //onRequestPermissionsResult()方法权限回调绑定到对象
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mTakePictureManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}