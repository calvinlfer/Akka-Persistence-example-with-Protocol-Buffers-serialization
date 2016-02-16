package com.experiments.calculator.models

object Models {

  // Commands: Do this action (potentially harmful)
  sealed trait Command
  case object Clear extends Command
  case class Add(value: Double) extends Command
  case class Subtract(value: Double) extends Command
  case class Divide(value: Double) extends Command
  case class Multiply(value: Double) extends Command
  case object PrintResult extends Command
  case object GetResult extends Command

  // Events: I have done this action (not harmful)
  sealed trait Event
  case object Reset extends Event
  case class Added(value: Double) extends Event
  case class Subtracted(value: Double) extends Event
  case class Divided(value: Double) extends Event
  case class Multiplied(value: Double) extends Event

  // Internal state for the calculator actor
  case class CalculationResult(result: Double = 0) {
    def reset = copy(result = 0)
    def add(value: Double) = copy(result = result + value)
    def subtract(value: Double) = copy(result = result - value)
    def multiply(value: Double) = copy(result = result * value)
    def divide(value: Double) = copy(result = result / value)
  }
}
