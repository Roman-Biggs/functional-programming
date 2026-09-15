import scala.collection.parallel.CollectionConverters._

object Workshop:

  // =========================================================================
  // ЗАВДАННЯ 2: РЕФАКТОРИНГ В ЧИСТЕ ФП (EXPRESSION-ORIENTED)
  // =========================================================================

  // 1. Готові рівні ризику бізнес-домену:
  sealed trait RiskLevel
  case object HighRisk extends RiskLevel
  case object MediumRisk extends RiskLevel
  case object LowRisk extends RiskLevel

  // 2. Ваше завдання: реалізувати чисту функцію класифікації як ВИРАЗ (без var).
  // У Scala звичайний `if-else` повертає значення (як тернарний оператор у C++/Java).
  // Умова:
  // - Якщо amount > 80.0 -> HighRisk
  // - Якщо amount > 50.0 -> MediumRisk
  // - Інакше -> LowRisk
  def categorize(amount: Double): RiskLevel = {
    if (amount > 80.0) {
      HighRisk
    }
    else if (amount > 50.0) {
      MediumRisk
    }
    else {
      LowRisk
    }
  }

  // 3. Реалізуйте чисту функцію для отримання коефіцієнта (як вираз):
  // HighRisk -> 1.5, MediumRisk -> 1.2, LowRisk -> 1.0
  def getMultiplier(level: RiskLevel): Double = level match {
    case HighRisk => 1.5
    case MediumRisk => 1.2
    case LowRisk => 1.0
  }

  @main def runWorkshop(): Unit =
    println("=== Практика 00: Вмикаємо мозок ===")

    // Генеруємо 100 транзакцій для швидких тестів (від 1 до 100)
    val data: Vector[Double] = (1 to 100).toVector.map(_.toDouble)

  // =========================================================================
  // ЗАВДАННЯ 1: ПАСТКА НА RACE CONDITION
  // =========================================================================
  // Студенте! Розкоментуй блок коду нижче і запусти програму (sbt "runMain Workshop") 3 рази підряд.
  // Чому результат кожного разу різний, хоча вхідні дані однакові?

    // Если проверить, как ведет себя функция без параллельных вычислений, результат получается одним и тем же
    // посольку функция чистая. При параллельных вычислениях происходит ситуация Race Condition, когда
    // результат вычисления с каждого ядра процессора пытается перезаписать одну и ту же колонку памяти своим значением

    var totalRisk = 0.0 // Зовнішній мутабельний стан (var)

    data.par.foreach { transactionId =>
      // Симуляція якогось обчислення (взято з Main.scala)
      val risk = math.sin(transactionId) * math.cos(transactionId) + math.tan(transactionId % 1.0)

      // Кілька потоків одночасно намагаються перезаписати totalRisk!
      totalRisk += risk
    }

    println(s"Сумарний ризик (через var): $totalRisk")

  // 4. Побудуйте чистий конвеєр обчислень.
  // Відфільтруйте транзакції (наприклад, залишіть лише > 50.0),
  // розрахуйте для них фінальний ризик (transactionId * multiplier) і знайдіть суму.

    val finalRiskSum = data
      .filter(amount => amount > 50.0)
      .map { amount =>
        val level = categorize(amount)
        val multiplier = getMultiplier(level)
        amount * multiplier
      }
      .sum

    println(s"Суммарный риск (через val): $finalRiskSum")