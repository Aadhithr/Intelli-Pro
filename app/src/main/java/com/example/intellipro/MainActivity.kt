package com.example.intellipro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.*
import java.net.URLEncoder

data class TaskItem(val task: String)

class TaskAdapter(private val taskList: MutableList<TaskItem>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.task_item, parent, false)) {
        private var mTaskView: TextView? = null

        init {
            mTaskView = itemView.findViewById(R.id.taskView)
        }

        fun bind(task: TaskItem) {
            mTaskView?.text = task.task
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TaskViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task: TaskItem = taskList[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = taskList.size
}

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: TaskAdapter
    private val items = ArrayList<TaskItem>()

    var projectGenerated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(items)
        recyclerView.adapter = adapter

        val inputEditText: EditText = findViewById(R.id.inputEditText)

        findViewById<View>(R.id.addButton).setOnClickListener {
            if (projectGenerated) {
                items.clear()
                adapter.notifyDataSetChanged()
                projectGenerated = false
            }
            val text = inputEditText.text.toString()
            if (text.isNotEmpty()) {
                items.add(TaskItem(text))
                adapter.notifyItemInserted(items.size - 1)
                inputEditText.text.clear()
            }
        }
        findViewById<View>(R.id.generateButton).setOnClickListener {
            val prompt = "I have these items at home: ${items.joinToString { it.task }}. Please create a project idea using these items and provide instructions to do so."
            generateOutput(prompt)
            projectGenerated = true
        }
    }

    private fun generateOutput(prompt: String) {
        findViewById<TextView>(R.id.outputText).text = ""
        val encodedPrompt = URLEncoder.encode(prompt, "UTF-8")
        val uri = URI.create("wss://backend.buildpicoapps.com/ask_ai_streaming?app_id=tough-natural&prompt=${encodedPrompt}")
        val client = object : WebSocketClient(uri) {
            override fun onOpen(handshakeData: ServerHandshake?) {
                // Here you can handle the actions after the connection is open
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                // Here you can handle the actions after the connection is closed
            }

            override fun onMessage(message: String?) {
                runOnUiThread {
                    val textView = findViewById<TextView>(R.id.outputText)
                    textView.text = textView.text.toString() + " " + message
                }
            }


            override fun onError(ex: Exception?) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error occurred: ${ex?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
        client.connect()
    }
}
