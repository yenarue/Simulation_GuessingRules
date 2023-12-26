package yenarue.simulation.ria

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

class MainViewModel(
    private val coroutineScope: CoroutineScope =
        CoroutineScope(Dispatchers.IO + CoroutineName("MainViewModel"))
) : ViewModel() {

    val ruleTryField = ObservableField<String>()
    val sequenceTryField = ObservableField<String>()

    private val _score = MutableLiveData(Preferences.getPreference("score"))
    val score: LiveData<Int> = _score

    private val _resultText = MutableLiveData("결과가 여기에 표시됩니다")
    val resultText: LiveData<String> = _resultText

    private val _errorEvent = MutableLiveData<Unit>()
    val errorEvent: LiveData<Unit> = _errorEvent

    override fun onCleared() {
        coroutineScope.cancel()
    }

    fun submit() {
        val ruleSolution = ruleTryField.get()
        val sequenceSolution = sequenceTryField.get()

        if (ruleSolution?.isBlank() == true && sequenceSolution?.isBlank() == true) {
            _errorEvent.postValue(Unit)
            return
        }

        val isRule = ruleSolution?.isNotBlank() ?: false
        val (result, score) = calculateResult(isRule, if (isRule) ruleSolution else sequenceSolution) ?: run {
            _errorEvent.postValue(Unit)
            return
        }
        _resultText.postValue("${if (result) "Pass" else "Fail"} ($score)")

        with(_score) {
            value?.plus(score)?.also {
                Preferences.savePreference("score", it)
                postValue(it)
            }
        }
    }

    private fun calculateResult(isRule: Boolean, solution: String?) : Pair<Boolean, Int>? {
        val solutions = solution?.split(":") ?: run {
            Log.e(TAG, "it is not a solution ($solution)")
            return null
        }
        val number = Integer.parseInt(solutions[0])
        val answer = solutions[1]

        val result = if (isRule) checkRule(number, answer) else checkSequence(number, answer)
        val score = if (isRule) getRuleScore(number, result) else 0
        return result to score
    }

    private fun checkSequence(ruleNumber: Int, answer: String) : Boolean {
        val sequence = answer.split("_").map { Integer.parseInt(it) }
        if (sequence.size < 5) return false

        return when(ruleNumber) {
            1 -> isPartOfFibonacciSequence(sequence)
            2 -> areAllElementsUnique(sequence)
            3 -> isStrictlyIncreasing(sequence)
            4 -> isPalindrome(sequence)
            5 -> areAllElementsMultiplesOfThree(sequence)
            else -> false
        }
    }

    private fun isFibonacciSequence(sequence: List<Int>): Boolean {
        // 수열의 길이 확인
        if (sequence.size < 2) return false

        // 피보나치 수열의 시작 확인
        if (sequence[0] != 0 || sequence[1] != 1) return false

        // 나머지 항목들을 확인
        for (i in 2 until sequence.size) {
            if (sequence[i] != sequence[i - 1] + sequence[i - 2]) return false
        }

        return true
    }

    private fun isPartOfFibonacciSequence(sequence: List<Int>): Boolean {
        if (sequence.isEmpty()) return false

        // 피보나치 수열 생성
        val fib = mutableListOf(0, 1)
        while (fib[fib.size - 1] < sequence.last()) {
            fib.add(fib[fib.size - 1] + fib[fib.size - 2])
        }

        // 입력된 수열의 시작점 찾기
        val startIndex = fib.indexOf(sequence.first())

        // 시작점이 없거나 수열이 피보나치 수열보다 길면 false 반환
        if (startIndex == -1 || startIndex + sequence.size > fib.size) return false

        // 입력된 수열이 피보나치 수열의 일부인지 확인
        for (i in sequence.indices) {
            if (sequence[i] != fib[startIndex + i]) {
                return false
            }
        }

        return true
    }


    private fun areAllElementsUnique(sequence: List<Int>): Boolean {
        return sequence.distinct().size == sequence.size
    }

    private fun isStrictlyIncreasing(sequence: List<Int>): Boolean {
        // 수열의 길이가 1 이하면 항상 true
        if (sequence.size <= 1) return true

        // 각 요소가 이전 요소보다 큰지 확인
        for (i in 1 until sequence.size) {
            if (sequence[i] <= sequence[i - 1]) {
                return false
            }
        }
        return true
    }

    private fun isPalindrome(sequence: List<Int>): Boolean {
        for (i in 0 until sequence.size / 2) {
            if (sequence[i] != sequence[sequence.size - 1 - i]) {
                return false
            }
        }
        return true
    }

    private fun areAllElementsMultiplesOfThree(sequence: List<Int>): Boolean {
        return sequence.all { it % 3 == 0 }
    }

    private fun checkRule(ruleNumber: Int, rule: String) = when (ruleNumber) {
        1 -> rule == "피보나치수열" || rule == "피보나치"
        2 -> rule == "모두다르다" || rule == "모두다름" || rule == "전부다르다" || rule == "전부다름" || rule == "중복제거" || rule == "중복제외" || rule == "중복없음"
        3 -> rule == "뒷숫자가앞숫자보다크다" || rule == "뒤의숫자가바로앞의숫자보다크다" || rule == "뒷숫자가바로앞숫자보다크다"
        4 -> rule == "대칭을이룬다" || rule == "좌우대칭" || rule == "대칭"
        5 -> rule == "3의배수"
        else -> false
    }

    private fun getRuleScore(ruleNumber: Int, result: Boolean): Int = when (ruleNumber) {
        1 -> if (result) 20 else -1
        2 -> if (result) 30 else -1
        3 -> if (result) 40 else -1
        4 -> if (result) 50 else -1
        5 -> if (result) 60 else -1
        else -> 0
    }


    val scoreInfo by lazy {
        val info = (1..5)
            .map { "Rule$it. " +
                    "Pass (${getRuleScore(it, true)})" +
                    " / Fail (${getRuleScore(it, false)})" }
            .joinToString("\n")
        MutableLiveData("<Score Map>\n$info")
    }
    companion object {
        private val TAG = "MainViewModel"
    }
}