package com.onespan.takeshisugai.qa_app1

import android.os.Bundle
//import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.auth.FirebaseUser
import android.content.Intent
import android.support.v7.widget.Toolbar

import android.support.design.widget.FloatingActionButton

import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import com.google.firebase.auth.FirebaseUser
import android.support.design.widget.Snackbar
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.util.Base64  //追加する
import android.widget.ListView


class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mToolbar: Toolbar
    private var mGenre = 0


    //override fun onCreate(savedInstanceState: Bundle?) {


        /*
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
    */

    // --- ここから ---
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter

    private var mGenreRef: DatabaseReference? = null

    private val mEventListener = object : ChildEventListener {
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

            val question = Question(title, body, name, uid, dataSnapshot.key ?: "",
                mGenre, bytes, answerArrayList)
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

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }
    // --- ここまで追加する ---

    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            mToolbar = findViewById(R.id.toolbar)
            setSupportActionBar(mToolbar)

            val fab = findViewById<FloatingActionButton>(R.id.fab)
            // --- ここから ---
            fab.setOnClickListener { view ->
                // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
                if (mGenre == 0) {
                    Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show()
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
                /*
                    // ジャンルを渡して質問作成画面を起動する
                    val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                    intent.putExtra("genre", mGenre)
                    startActivity(intent)

                    */
                }
            }
            // --- ここまで修正 ---

           /*
            fab.setOnClickListener { _ ->
                // ログイン済みのユーザーを取得する
                val user = FirebaseAuth.getInstance().currentUser

                // ログインしていなければログイン画面に遷移させる
                if (user == null) {
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
            */

            /*
        // --- ここから ---
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { _ ->
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            // ログインしていなければログイン画面に遷移させる
            if (user == null) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        */

            // ナビゲーションドロワーの設定
            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            val toggle = ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name)
            drawer.addDrawerListener(toggle)
            toggle.syncState()

            val navigationView = findViewById<NavigationView>(R.id.nav_view)
            navigationView.setNavigationItemSelectedListener(this)

            // --- ここまで修正 ---
            /*
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
                */
        }

    // }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /*
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
*/

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.nav_hobby) {
            mToolbar.title = "趣味"
            mGenre = 1
        } else if (id == R.id.nav_life) {
            mToolbar.title = "生活"
            mGenre = 2
        } else if (id == R.id.nav_health) {
            mToolbar.title = "健康"
            mGenre = 3
        } else if (id == R.id.nav_compter) {
            mToolbar.title = "コンピューター"
            mGenre = 4
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    // 2nd/Feb added
    /*
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }
*/
    // 2nd/Feb no2  added
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        // 1:趣味を既定の選択とする
        if(mGenre == 0) {
            onNavigationItemSelected(navigationView.menu.getItem(0))
        }
    }
}


