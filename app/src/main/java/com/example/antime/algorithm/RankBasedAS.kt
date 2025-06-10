package com.example.antime.algorithm

import kotlin.math.pow
import kotlin.random.Random

class RankBasedAS(private val activities: List<Activities>, private val listMeetings : HashMap<String, MutableList<Int>>) {
    private val pheromone : MutableMap<String,Double> = mutableMapOf()
    private val alpha = 1.0
    private val beta = 2.0
    private val rho = 0.1
    private val q = 100.0
    private val numAnts = 20
    private val numIteratrions= 50
    private val rankFactor = 5

    fun schedule() : List<Assignment>{
        var globalBest :List<Assignment> = emptyList()
        var bestScore = Double.MIN_VALUE

        for (i in 0 until numIteratrions){
            println("Iterasi $i:")

            val antSolutions = mutableListOf<Pair<List<Assignment>,Double>>()
            repeat(numAnts){
                val assignment = constructSolution()
                val score = evaluateSolution(assignment)
                antSolutions.add(assignment to score)
                println("Ant $it: Score = $score")
            }

            antSolutions.sortByDescending { it.second }
            println("Score terbaik pada iterasi ini: ${antSolutions[0].second}")

            if (antSolutions[0].second > bestScore){
                globalBest = antSolutions[0].first
                bestScore = antSolutions[0].second
                println("New global best score found: $bestScore")
            }
            updatePheromone(antSolutions.take(rankFactor))
            println("Pheromone updated for top ${rankFactor} solutions.")
        }
        println("Final best score: $bestScore")
        return globalBest
    }

    private fun constructSolution(): List<Assignment> {
        val assigned = mutableListOf<Assignment>()
        val used = mutableMapOf<String, MutableSet<String>>()

        activities.shuffled().forEach { activities ->
            val options = mutableListOf<Assignment>()

            for (day in days) {
                for (hour in timeSlots) {
                    if (listMeetings.containsKey(day) && listMeetings[day]?.contains(hour)==true) continue // meeting constraint
                    if (hour == 12) continue // lunch break
                    for (room in rooms) {
                        val key = "${day}_${hour}_${room}"
                        val roomConflict = assigned.any {assignment->
                            assignment.room == room && assignment.startHour==hour && assignment.day==day
                        }
                        if (!used.getOrPut(key) { mutableSetOf() }.contains(activities.pic) &&
                            !used.getOrPut(key) { mutableSetOf() }.contains(activities.programmer)&&
                            !roomConflict) {
                            options.add(Assignment(activities, room, day, hour))
                        }
                    }
                }
            }

            if (options.isNotEmpty()) {
                val chosen = selectAssignmentWithPheromone(options)
                val key = "${chosen.day}_${chosen.startHour}_${chosen.room}"
                used.getOrPut(key) { mutableSetOf() }.add(chosen.activities.pic)
                used[key]?.add(chosen.activities.programmer)
                assigned.add(chosen)
            }
        }

        return assigned
    }

    private fun selectAssignmentWithPheromone(options: MutableList<Assignment>): Assignment {
        val scores = options.map {
            val key = "${it.day}_${it.startHour}_${it.room}_${it.activities.pic}_${it.activities.programmer}"
            val tau = pheromone[key] ?: 1.0
            val eta = 1.0
            tau.pow(alpha) * eta.pow(beta)
        }

        val total = scores.sum()
        val probabilities = scores.map { it/total }
        val rand = Random.nextDouble()
        var cumulative =0.0
        for ((index, prob) in probabilities.withIndex()){
            cumulative+=prob
            if (rand<= cumulative) return options[index]
        }
        return options.last()
    }

    private fun evaluateSolution(solution: List<Assignment>): Double {
        val violations = solution.sumOf { assignment ->
            countViolations(assignment, solution)
        }
        return q / (1 + violations)
    }

    private fun countViolations(assignment: Assignment, solution: List<Assignment>): Int {
        var violations = 0
        solution.filter { it!= assignment&&
        it.day == assignment.day&&
        it.startHour == assignment.startHour
        }.forEach{conflict->
            if (conflict.room == assignment.room) violations++
            if (conflict.activities.pic==assignment.activities.pic) violations++
            if (conflict.activities.programmer==assignment.activities.programmer) violations++
        }
        return violations
    }


    private fun updatePheromone(topRanked: List<Pair<List<Assignment>, Double>>) {
        pheromone.entries.forEach{ pheromone[it.key] = it.value * (1-  rho)}

        for ((rank, pair) in topRanked.withIndex()) {
            val (assignments, _) = pair
            val delta = q * (rankFactor - rank)
            assignments.forEach {
                val key = "${it.day}_${it.startHour}_${it.room}_${it.activities.pic}_${it.activities.programmer}"
                pheromone[key] = (pheromone[key] ?: 0.0) + delta
            }
        }
    }
}