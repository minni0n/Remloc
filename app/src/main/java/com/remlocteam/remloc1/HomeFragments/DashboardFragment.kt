package com.remlocteam.remloc1.HomeFragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.remlocteam.remloc1.MapActivity
import com.remlocteam.remloc1.R
import com.remlocteam.remloc1.databinding.FragmentDashboardBinding
import kotlin.math.roundToInt

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        setupQuickActions()
        loadDashboardData()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupQuickActions() {
        binding.openPlacesButton.setOnClickListener {
            startActivity(Intent(requireContext(), MapActivity::class.java))
        }

        binding.openActionsButton.setOnClickListener {
            (activity as? com.remlocteam.remloc1.HomeActivity)?.replaceFragment(
                ActionsFragment(),
                getString(R.string.actions)
            )
        }

        binding.openSettingsButton.setOnClickListener {
            (activity as? com.remlocteam.remloc1.HomeActivity)?.replaceFragment(
                SettingsFragment(),
                getString(R.string.settings)
            )
        }
    }

    private fun loadDashboardData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference(uid)

        db.child("Places").get().addOnSuccessListener { placesSnapshot ->
            val placesCount = placesSnapshot.childrenCount.toInt()
            binding.placesCount.text = placesCount.toString()
            binding.dashboardSubtitle.text = getString(R.string.dashboard_summary, placesCount)
        }

        db.child("Actions").get().addOnSuccessListener { actionsSnapshot ->
            var totalActions = 0
            var enabledActions = 0
            var smsActions = 0

            actionsSnapshot.children.forEach { actionTypeNode ->
                actionTypeNode.children.forEach { actionNode ->
                    totalActions += 1
                    val isEnabled = actionNode.child("turnOn").getValue(Boolean::class.java) ?: true
                    if (isEnabled) enabledActions += 1

                    val type = actionNode.child("actionType").value?.toString()
                    if (type == "Sms") smsActions += 1
                }
            }

            binding.actionsCount.text = totalActions.toString()
            binding.activeCount.text = enabledActions.toString()
            binding.smsCount.text = smsActions.toString()

            val activeRatio = if (totalActions == 0) 0 else ((enabledActions.toFloat() / totalActions) * 100).roundToInt()
            binding.activeProgress.progress = activeRatio
            binding.activeProgressLabel.text = getString(R.string.dashboard_active_ratio, activeRatio)

            val statusText = if (enabledActions > 0) {
                getString(R.string.dashboard_tracking_ready)
            } else {
                getString(R.string.dashboard_tracking_disabled)
            }
            binding.statusChip.text = statusText
            val chipColor = if (enabledActions > 0) R.color.apple_success else R.color.apple_warning
            binding.statusChip.setChipBackgroundColorResource(chipColor)
            binding.statusChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }
}
