package se.infomaker.frt.ui.adapter

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.navigaglobal.mobile.R
import javax.inject.Inject

class CentrePointAdapter @Inject constructor() : RecyclerView.Adapter<MainViewHolder>() {

    private lateinit var mContext: Context
    var centerPointList = mutableListOf<CentrePointDTO>()

    fun setList(list: List<CentrePointDTO>) {
        this.centerPointList = list.toMutableList()
        notifyDataSetChanged()
    }
    fun setContext(context: Context){
        this.mContext=context

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val binding = inflater.inflate(R.layout.centrepoint_layout_adapter, parent, false)
        return MainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {

        val data = centerPointList.get(position)
        holder.name.setText(data.category)
        Glide.with(holder.itemView.context).load(data.img_url).into(holder.img)
        holder.img.setOnClickListener {
           // Log.d("CenterPointAdapter"," "+ Gson().toJson(centerPointList[position]))
            try {
            val webpage: Uri = Uri.parse(centerPointList.get(position).android_url)
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            mContext.startActivity(intent)

            } catch (anfe: ActivityNotFoundException) {

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(centerPointList.get(position).web_url))
                if (null != intent.resolveActivity(mContext.getPackageManager())) {
                    mContext.startActivity(intent)
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return centerPointList.size
    }
}

class MainViewHolder(val binding: View) : RecyclerView.ViewHolder(binding) {

    val img=binding.findViewById<ImageView>(R.id.imageview)
    val name=binding.findViewById<TextView>(R.id.name)
}