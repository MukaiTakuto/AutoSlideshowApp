package jp.techacademy.takuto.mukai.autoslideshowapp

import android.Manifest
import android.R.attr.data
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import java.util.*


class MainActivity : AppCompatActivity() {
    var mSlideCounter: Timer? = null
    var mTimerText: TextView? = null
    var mStartButton: Button? = null
    var mNextButton: Button? = null
    var mBackbutton: Button? = null
    var mImageView: ImageView? = null
    var imageUriArray = ArrayList<Uri>()

    var CountNum = 0
    var mHandler = Handler()

//    fun getMax(): Int {
//        return data
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mTimerText = findViewById<View>(R.id.timer) as TextView
        mStartButton = findViewById<View>(R.id.start_button) as Button
        mNextButton = findViewById<View>(R.id.next_button) as Button
        mBackbutton = findViewById<View>(R.id.back_button) as Button
        mImageView = findViewById<View>(R.id.imageView) as ImageView
        //起動した時にパーミッションの許可状態を確認
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //許可されている
                Log.d("ANDROID", "許可されている")
                contentsInfo
            } else {
                Log.d("ANDROID", "許可されていない")
                //ダイアログ表示
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
        } else {
            contentsInfo
        }
        mStartButton!!.setOnClickListener {
            //画像の取得に成功しているかつ画像が1枚以上あったらタイマーを作動
            if (imageUriArray.size != 0) {
                if (mSlideCounter == null) {
                    //タイマーの作成
                    mSlideCounter = Timer()
                    //カウンターを作成したら，ボタンを停止に変える
                    mStartButton!!.text = "停止"

                     mSlideCounter!!.schedule(object : TimerTask() {
                        override fun run() {
                            CountNum += 1
                            mHandler.post {
                                val num = CountNum % imageUriArray.size
                                mImageView!!.setImageURI(imageUriArray[num])
                                mTimerText!!.text = String.format(
                                    "%d枚目を表示中",
                                    num + 1
                                )
                            }
                        }
                    }, 2000, 2000)
                } else {
                    //カウンターを止める
                    mSlideCounter!!.cancel()
                    mSlideCounter = null
                    mStartButton!!.text = "再生"
                    }
            }
        }
        mNextButton!!.setOnClickListener { //画像の取得に成功しているかつ画像が1枚以上あったら次へ
            if (imageUriArray.size != 0) {
                if (mSlideCounter == null) {
                    CountNum += 1
                    val num = CountNum % imageUriArray.size
                    mImageView!!.setImageURI(imageUriArray[num])
                    mTimerText!!.text = String.format("%d枚目を表示中", num + 1)
                }
            } else {
                mTimerText!!.text = String.format("写真へのアクセスを許可した後に，画像を1枚以上追加してください")
            }
        }
        mBackbutton!!.setOnClickListener { //画像の取得に成功しているかつ画像が1枚以上あったら戻る
            if (imageUriArray.size != 0) {
                if (mSlideCounter == null) {
                    if (CountNum == 0) {
                        CountNum = Int.MAX_VALUE

                        val num = CountNum % imageUriArray.size
                        mImageView!!.setImageURI(imageUriArray[num])
                        mTimerText!!.text = String.format("%d枚目を表示中", num + 0)
                    }
                   else {

                        CountNum -= 1

                        val num = CountNum % imageUriArray.size
                        mImageView!!.setImageURI(imageUriArray[num])
                        mTimerText!!.text = String.format("%d枚目を表示中", num + 1)
                    }
                }
                } else {
                    mTimerText!!.text = String.format("写真へのアクセスを許可した後に，画像を1枚以上追加してください")
                }
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                contentsInfo
            }
            else -> mTimerText!!.text = String.format("写真へのアクセスを許可してください")
        }
    }//indexからIDを取得

    //画像の情報を取得
    private val contentsInfo: Unit
        private get() {
            //画像の情報を取得
            val resolver = contentResolver
            val cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
            )
            if (cursor.moveToFirst()) {
                do {
                    //indexからIDを取得
                    val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    val id = cursor.getLong(fieldIndex)
                    val imageURi =
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    Log.d("ANDROID", "URI:$imageURi")
                    imageUriArray.add(imageURi)
                } while (cursor.moveToNext())
            }
            mImageView!!.setImageURI(imageUriArray[0])
            cursor.close()
        }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
    }
}