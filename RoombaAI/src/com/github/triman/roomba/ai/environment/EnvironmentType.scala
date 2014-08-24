package com.github.triman.roomba.ai.environment

abstract sealed class EnvironmentType

case object UnknownEnvironment extends EnvironmentType
case object OutsideMapEnvironment extends EnvironmentType
case object FloorEnvironment extends EnvironmentType
case object ObstacleEnvironment extends EnvironmentType
