package se.infomaker.iap.provisioning.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.navigaglobal.mobile.R
import se.infomaker.iap.provisioning.credentials.Credential
import se.infomaker.iap.theme.view.ThemeableMaterialButton

class CredentialAdapter(private val credentials: LiveData<List<Credential>>, var onCredentialSelected: ((Credential) -> Unit)?) : RecyclerView.Adapter<CredentialAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.credential_item
    }

    override fun getItemCount(): Int  = credentials.value?.size ?: 0

    override fun onBindViewHolder(holder: VH, position: Int) {
        credentials.value?.let {
            holder.bind(it[position], onCredentialSelected)
        }
    }

    class VH(view: View): RecyclerView.ViewHolder(view) {
        private val button: ThemeableMaterialButton = view.findViewById(R.id.button)

        fun bind(credential: Credential, onCredentialSelected: ((Credential) -> Unit)?) {
            button.text = credential.username
            button.setOnClickListener {
                onCredentialSelected?.invoke(credential)
            }
        }
    }
}
