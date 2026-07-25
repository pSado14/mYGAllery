package com.sado.mygallery.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sado.mygallery.data.local.Rule
import com.sado.mygallery.data.local.RuleDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val ruleDao: RuleDao
) : ViewModel() {

    val rules: StateFlow<List<Rule>> = ruleDao.getAllRules()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addRule(aiLabel: String, targetFolderName: String) {
        if (aiLabel.isBlank() || targetFolderName.isBlank()) return
        viewModelScope.launch {
            ruleDao.insertRule(
                Rule(
                    aiLabel = aiLabel.trim(),
                    targetFolderName = targetFolderName.trim(),
                    isActive = true
                )
            )
        }
    }

    fun toggleRule(rule: Rule, isActive: Boolean) {
        viewModelScope.launch {
            ruleDao.updateRule(rule.copy(isActive = isActive))
        }
    }

    fun deleteRule(rule: Rule) {
        viewModelScope.launch {
            ruleDao.deleteRule(rule)
        }
    }
}
