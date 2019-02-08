package com.onespan.takeshisugai.qa_app1

//package jp.techacademy.taro.kirameki.qa_app

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ListView
import com.google.android.gms.common.GooglePlayServicesNotAvailableException

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*

import java.util.HashMap

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference

    // private var FavoritePATH = "./temp1"
    private var FavoritePATH = "temp1"


    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                // --- ここから ---
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
                // --- ここまで ---
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef =
                dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid)
                    .child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)


        this.button2.setOnClickListener {
                // View1 ->
            val user = FirebaseAuth.getInstance().currentUser  // ユーザー取得
            var isFavorite = false
            if (user == null) {
                // ユーザーが取れない＝未ログインならボタン非表示
                // this.button2.visibility = View.GONE
                this.button2.setVisibility(View.GONE)
                //this.button2.visibility = View.INVISIBLE
                // this.button2.setVisibility(View.GONE)
            } else {
                // お気に入りのデータを取得する
                FirebaseDatabase
                    .getInstance()
                    .reference
                    .child(FavoritePATH)
                    //.child("./temp1")
                    .child(user.uid)
                    .child(mQuestion.uid)

                //val dataBaseReference2 = FirebaseDatabase.getInstance().reference
                dataBaseReference.child(user.uid).child(mQuestion.uid).push()


                //｀mDatabaseReference｀がFirebaseDatabaseのルートのリファレンスだとすると、
                // ｀mDatabaseReference.child( ユーザ名 ).child( 質問ID ).push()｀ としてリファレンスを保存します。

                    .addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            // お気に入りがあった場合、テキストを解除に変更
                            isFavorite = true
                            this@QuestionDetailActivity.button2.text = "お気に入りを解除"
                        }
                    })

                this.button2.setOnClickListener {
                    if (isFavorite) {
                        // すでにお気に入りなら、お気に入り解除処理をする
                        FirebaseDatabase
                            .getInstance()
                            .reference
                            .child(FavoritePATH)
                            .child(user.uid)
                            .child(mQuestion.uid)
                            .removeValue()
                        isFavorite = true
                        this@QuestionDetailActivity.button2.text = "お気に入りに登録"
                    } else {
                        // お気に入りでなければ、お気に入りに登録する処理をする
                        val data = HashMap<String, Int>()
                        data["genre"] = mQuestion.genre
                        FirebaseDatabase
                            .getInstance()
                            .reference
                            .child(FavoritePATH)
                            .child(user.uid)
                            .child(mQuestion.uid)
                            .push()
                            .setValue(data)
                    }
                }
            }
        }
    }

    /*
    this.button2.setOnClickListener {



    }
    */
    //button2.setOnClickListener { Log.v(TAG, "clicked") }


    /*
    button2.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.v(TAG, "clicked");
        }
    });

*/

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                // TODO:
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)
    }
    */

}
