package edu.minesweeper

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView


class TopActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var list: List<Pair<String, Long>>

    inner class TopAdapter : RecyclerView.Adapter<TopAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)

            return ViewHolder(inflater.inflate(R.layout.top_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemView.apply {
                val item = list[position]

                findViewById<TextView>(R.id.player_item).text = item.first
                findViewById<TextView>(R.id.score_item).text = TopList.humanizedFormat(item.second)
            }

        }

        override fun getItemCount() = list.size
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top)

        list = chooseMode(GameMode.EASY)

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            adapter = TopAdapter()
            layoutManager = LinearLayoutManager(this@TopActivity)
        }

        findViewById<BottomNavigationView>(R.id.bottom_navigation)
            .setOnNavigationItemSelectedListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_activity_tools, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear_top -> {
                TopList.clear()
                list = chooseMode(GameMode.EASY)
                recyclerView.adapter!!.notifyDataSetChanged()
                true
            }
            else -> false
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        list = chooseMode(
            when (item.itemId) {
                R.id.menu_item_easy -> GameMode.EASY
                R.id.menu_item_medium -> GameMode.MEDIUM
                R.id.menu_item_hard -> GameMode.HARD
                else -> return false
            }
        )
        recyclerView.adapter!!.notifyDataSetChanged()

        return true
    }

    private fun chooseMode(mode: GameMode) =
            TopList.list[mode]!!
                .toList()
                .sortedWith(compareBy { it.second })

}