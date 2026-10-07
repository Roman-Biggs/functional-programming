package runner.custom.bots

import runner.*
import runner.bots.CyberBot

import scala.util.Random

class Stage1ReflexBot(seed: Long = 202L) extends CyberBot:
  private val rng = new Random(seed)

  override def name: String = "Stage 1 ReflexBot"

  override def decide(observation: Observation): Action =
    val hero = observation.hero
    val currentLane = hero.lane
    val nextSlice = observation.upcoming.head

    // Препятствия в позициях: снизу, посередине, сверху
    val hasLow = nextSlice.hasObstacleAt(currentLane, Height.Low)
    val hasMid = nextSlice.hasObstacleAt(currentLane, Height.Mid)
    val hasHigh = nextSlice.hasObstacleAt(currentLane, Height.High)

    // В зависимости от того, какие препятствия будут, выбираем действие бота
    (hasLow, hasMid, hasHigh) match
      // 1. Комбинация (низ + верх). Середина (_) не имеет значения.
      case (true, _, true) =>
        // Контроль выбора полосы (чтобы не выйти за пределы)
        currentLane match
          case Lane.Left => Action.MoveRight
          case Lane.Right => Action.MoveLeft
          case Lane.Center =>
            // Можно двигаться как влево, так и вправо
            val escapes = List(Action.MoveLeft, Action.MoveRight)
            escapes(rng.nextInt(escapes.length))

      // 2. Препятствие только снизу -> Прыжок
      case (true, _, false) => Action.Jump

      // 3. Препятствие посередине -> Присед
      case (false, true, _) => Action.Duck

      // 4. Все остальные случае -> Бег прямо
      case _ => Action.KeepRunning