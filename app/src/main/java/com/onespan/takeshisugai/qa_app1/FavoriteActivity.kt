package com.onespan.takeshisugai.qa_app1

//package jp.techacademy.taro.kirameki.qa_app


import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Base64
import android.util.Log
import android.widget.ListView

import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_favorite.*


import com.google.firebase.database.*

class FavoriteActivity : AppCompatActivity()  {

    private lateinit var mToolbar: Toolbar
    private var mGenre = 0

    private var FavoritePATH = "temp1"

    // --- ここから ---
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mQuestionArrayList_All: ArrayList<Question>      // 2019/02/12 E.Nozaki すべての質問のリスト
    private lateinit var mQuestionArrayList_Favorite: ArrayList<Question> // 2019/02/12 E.Nozaki お気に入りの質問のリスト
    private lateinit var mFavoriteQuestionIDs: ArrayList<String>          // 2019/02/12 E.Nozaki お気に入りの質問のIDのリスト
    private lateinit var mAdapter: QuestionsListAdapter

    private var mGenreRef: DatabaseReference? = null

    private val mEventListener = object : ChildEventListener {

        override fun onCancelled(p0: DatabaseError) {
            // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onChildRemoved(p0: DataSnapshot) {
            // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""
            val imageString = map["image"] ?: ""
            val bytes =
                if (imageString.isNotEmpty()) {
                    Base64.decode(imageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }

            val answerArrayList = ArrayList<Answer>()
            val answerMap = map["answers"] as Map<String, String>?
            if (answerMap != null) {
                for (key in answerMap.keys) {
                    val temp = answerMap[key] as Map<String, String>
                    val answerBody = temp["body"] ?: ""
                    val answerName = temp["name"] ?: ""
                    val answerUid = temp["uid"] ?: ""
                    val answer = Answer(answerBody, answerName, answerUid, key)
                    answerArrayList.add(answer)
                }
            }

            val question = Question(
                title, body, name, uid, dataSnapshot.key ?: "",
                mGenre, bytes, answerArrayList
            )
            mQuestionArrayList_All.add(question) // 2019/02/12 E.Nozaki すべての質問を mQuestionArrayList_All にまとめて格納しておきます。
            filterFavoriteQuestions()  // 2019/02/12 E.Nozaki mQuestionArrayList_All の中からお気に入りの質問を抽出して、mQuestionArrayList_Favorite に格納します。
            mAdapter.notifyDataSetChanged()

            Log.d("Eiichi", "mQuestionArrayList_Allのサイズ=" + mQuestionArrayList_All.size + ", 質問=" + question.title)
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            // 変更があったQuestionを探す
            for (question in mQuestionArrayList_Favorite) { // 2019/02/12 E.Nozaki
                if (dataSnapshot.key.equals(question.questionUid)) {
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            question.answers.add(answer)
                        }
                    }

                    mAdapter.notifyDataSetChanged()
                }
            }
        }

        /*
        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }
    :?
  */

    }
    ///+++++++++

    // 2019/02/12 E.Nozaki 下記は「お気に入り」の情報を取得するためのリスナーの定義です。

    private val mFavoriteEventListener = object : ChildEventListener {

        override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {
            if(snapshot.value==true){
                mFavoriteQuestionIDs.add(snapshot.key.toString())
            } else {
                mFavoriteQuestionIDs.remove(snapshot.key.toString())
            }

            filterFavoriteQuestions()  // 2019/02/12 E.Nozaki mQuestionArrayList_All の中からお気に入りの質問を抽出して、mQuestionArrayList_Favorite に格納します。
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(snapshot: DataSnapshot, p1: String?) {
            if(snapshot.value==true){
                mFavoriteQuestionIDs.add(snapshot.key.toString())
            } else {
                mFavoriteQuestionIDs.remove(snapshot.key.toString())
            }

            filterFavoriteQuestions()  // 2019/02/12 E.Nozaki mQuestionArrayList_All の中からお気に入りの質問を抽出して、mQuestionArrayList_Favorite に格納します。
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            mFavoriteQuestionIDs.remove(snapshot.key.toString())
            filterFavoriteQuestions()  // 2019/02/12 E.Nozaki mQuestionArrayList_All の中からお気に入りの質問を抽出して、mQuestionArrayList_Favorite に格納します。
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildMoved(snapshot: DataSnapshot, p1: String?) { }
        override fun onCancelled(error: DatabaseError) {}
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        lateinit var mQuestion: Question

        // →まず、onCreate関数を作成してください。
        //　次にその中で、mEventListenerを呼び出す処理を書いてください


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite) //これは必要でしょうか？

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras



        // activity_favorite.
        //  super.onCreate(savedInstanceState)
        //  setContentView(R.layout.activity_favorite) //これは必要でしょうか？
        //  setContentView(R.layout.activity_main)

        // deleted 1oth/Feb.
        // mToolbar = findViewById(R.id.toolbar)
        // setSupportActionBar(mToolbar)


        /*
        val fab = findViewById<FloatingActionButton>(R.id.fab)


        fab.setOnClickListener { view ->
            // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
            if (mGenre == 0) {
                //Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show()
            } else {

            }
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // ジャンルを渡して質問作成画面を起動する
                val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                intent.putExtra("genre", mGenre)
                startActivity(intent)
            }
        }
  */
        // ナビゲーションドロワーの設定
        /*
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
     */

        //   val navigationView = findViewById<NavigationView>(R.id.nav_view)


        // 9th/Fev
        // navigationView.NavigationItemSelectedListener(this)

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // 2019/02/12 下記の3つのリストを初期化します。
        mQuestionArrayList_All = ArrayList<Question>()      // 2019/02/12 E.Nozaki
        mQuestionArrayList_Favorite = ArrayList<Question>() // 2019/02/12 E.Nozaki
        mFavoriteQuestionIDs = ArrayList<String>()          // 2019/02/12 E.Nozaki

        // ListViewの準備
        mListView = findViewById(R.id.listView)
        mAdapter = QuestionsListAdapter(this)
        mAdapter.setQuestionArrayList(mQuestionArrayList_Favorite) // 2019/02/12 E.Nozaki
        mListView.adapter = mAdapter // 2019/02/12 E.Nozaki
        mAdapter.notifyDataSetChanged()

        mListView.setOnItemClickListener { parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList_Favorite[position]) // 2019/02/12 E.Nozaki
            startActivity(intent)
        }

        /* 10th added by T. Sugai  */

        /*
        // --- ここから ---
        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        mListView.adapter = mAdapter

        // 選択したジャンルにリスナーを登録する
        if (mGenreRef != null) {
            mGenreRef!!.removeEventListener(mEventListener)
        }
        mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
        mGenreRef!!.addChildEventListener(mEventListener)
        // --- ここまで追加する ---
*/

        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            // ログインしていなければログイン画面に遷移させる
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        } else {

            // 2019/02/11 E.Nozaki このユーザーがこの質問を「お気に入りに登録したかどうか」をセットしておく Firebase上の場所にリスナーを設定します。

            mDatabaseReference.child(FavoritePATH).child(user.uid).addChildEventListener(mFavoriteEventListener)

            // 2019/02/12 E.Nozaki すべてのジャンルの質問を Firebase から取得します。

            mDatabaseReference.child(ContentsPATH).child("1").addChildEventListener(mEventListener)
            mDatabaseReference.child(ContentsPATH).child("2").addChildEventListener(mEventListener)
            mDatabaseReference.child(ContentsPATH).child("3").addChildEventListener(mEventListener)
            mDatabaseReference.child(ContentsPATH).child("4").addChildEventListener(mEventListener)

            // ジャンルを渡して質問作成画面を起動する
            //val intent = Intent(applicationContext, QuestionSendActivity::class.java)
            //intent.putExtra("genre", mGenre)
            //startActivity(intent)
        }

        // adviced by Mr. Hori
        // mGenreRef = mDatabaseReference.child(FavoritePATH).child(mGenreRef.toString())
        // mGenreRef = mDatabaseReference.child(FavoritePATH).child(user!!.uid).child(mQuestion.uid)
        // mGenreRef!!.addChildEventListener(mEventListener)

        // .child(user.uid)
    }

    // 2019/02/12 E.Nozaki 下記は、mQuestionArrayList_All の中からお気に入りの質問を抽出して、mQuestionArrayList_Favorite に格納するメソッドの定義です。

    private fun filterFavoriteQuestions() {

        mQuestionArrayList_Favorite.clear()

        for(question in mQuestionArrayList_All) {

            if(mFavoriteQuestionIDs.contains(question.uid)) { // 2019/02/12 E.Nozaki この質問が「お気に入り」に登録されているかどうかをチェック。
                mQuestionArrayList_Favorite.add(question)
            }
        }
    }
}



/*
package com.onespan.takeshisugai.qa_app1

//package jp.techacademy.taro.kirameki.qa_app


import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Base64
import android.widget.ListView

import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_favorite.*


import com.google.firebase.database.*

class FavoriteActivity : AppCompatActivity()  {

    private lateinit var mToolbar: Toolbar
    private var mGenre = 0

    private var FavoritePATH = "temp1"

    // --- ここから ---
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter

    private var mGenreRef: DatabaseReference? = null

    private val mEventListener = object : ChildEventListener {

        override fun onCancelled(p0: DatabaseError) {
            // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onChildRemoved(p0: DataSnapshot) {
           // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""
            val imageString = map["image"] ?: ""
            val bytes =
                if (imageString.isNotEmpty()) {
                    Base64.decode(imageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }

            val answerArrayList = ArrayList<Answer>()
            val answerMap = map["answers"] as Map<String, String>?
            if (answerMap != null) {
                for (key in answerMap.keys) {
                    val temp = answerMap[key] as Map<String, String>
                    val answerBody = temp["body"] ?: ""
                    val answerName = temp["name"] ?: ""
                    val answerUid = temp["uid"] ?: ""
                    val answer = Answer(answerBody, answerName, answerUid, key)
                    answerArrayList.add(answer)
                }
            }

            val question = Question(
                title, body, name, uid, dataSnapshot.key ?: "",
                mGenre, bytes, answerArrayList
            )
            mQuestionArrayList.add(question)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            // 変更があったQuestionを探す
            for (question in mQuestionArrayList) {
                if (dataSnapshot.key.equals(question.questionUid)) {
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            question.answers.add(answer)
                        }
                    }

                    mAdapter.notifyDataSetChanged()
                }
            }
        }


        // override fun onChildRemoved(p0: DataSnapshot) {
      //
       // }
      //
      //  override fun onChildMoved(p0: DataSnapshot, p1: String?) {
     //
     //   }
     //
     //   override fun onCancelled(p0: DatabaseError) {
//
//        }
   // }


    }
    ///+++++++++


    override fun onCreate(savedInstanceState: Bundle?) {

        lateinit var mQuestion: Question

        // →まず、onCreate関数を作成してください。
        //　次にその中で、mEventListenerを呼び出す処理を書いてください


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite) //これは必要でしょうか？

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras



        // activity_favorite.
      //  super.onCreate(savedInstanceState)
      //  setContentView(R.layout.activity_favorite) //これは必要でしょうか？
       //  setContentView(R.layout.activity_main)

        // deleted 1oth/Feb.
        // mToolbar = findViewById(R.id.toolbar)
        // setSupportActionBar(mToolbar)




     //   val navigationView = findViewById<NavigationView>(R.id.nav_view)


        // 9th/Fev
        // navigationView.NavigationItemSelectedListener(this)

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mListView = findViewById(R.id.listView)
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        mListView.setOnItemClickListener { parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }



        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            // ログインしていなければログイン画面に遷移させる
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        } else {
            // ジャンルを渡して質問作成画面を起動する
            //val intent = Intent(applicationContext, QuestionSendActivity::class.java)
            //intent.putExtra("genre", mGenre)
            //startActivity(intent)
        }



        // adviced by Mr. Hori
        // mGenreRef = mDatabaseReference.child(FavoritePATH).child(mGenreRef.toString())
       // mGenreRef = mDatabaseReference.child(FavoritePATH).child(user!!.uid).child(mQuestion.uid)
       // mGenreRef!!.addChildEventListener(mEventListener)

           // .child(user.uid)



    }




}
        */
