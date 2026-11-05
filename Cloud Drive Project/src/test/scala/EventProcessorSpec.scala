import org.scalatest.funsuite.AnyFunSuite
import entities.{CloudDrive, CloudFile}
import functionality.{EventProcessor, Upload, MoveToTrash, EmptyTrash, ApplyPromo, InsufficientSpace, CloudLogic}

class EventProcessorSpec extends AnyFunSuite {

  test("Processor: Apply UPLOAD successfully") {
    val initialDrive = CloudDrive("user1", 500.0)
    val event = Upload("holiday_video", 200.0, "VID")

    val result = EventProcessor.applyEvent(initialDrive, event)

    assert(result.isRight)
    val newDrive = result.toOption.get
    assert(newDrive.activeFiles.head.name == "holiday_video")
    assert(newDrive.activeFiles.head.fileType == "vid") // Ожидаем форматированный тип
  }

  test("Processor: Apply UPLOAD fails (InsufficientSpace invariant)") {
    val initialDrive = CloudDrive("user1", 100.0)
    val event = Upload("huge_backup", 150.0, "OTHER")

    val result = EventProcessor.applyEvent(initialDrive, event)

    assert(result.isLeft)
    assert(result.left.toOption.get == InsufficientSpace("huge_backup", 150.0, 100.0))
  }

  test("Processor: Apply TRASH successfully") {
    // Диск с двумя файлами с одинаковым именем, но разными расширениями
    val fileToKeep = CloudFile("old_doc", 50.0, "pdf")
    val fileToTrash = CloudFile("old_doc", 50.0, "doc")

    val initialDrive = CloudDrive("user1", 500.0, activeFiles = List(fileToKeep, fileToTrash))

    // Событие удаления нацелено только на DOC
    val event = MoveToTrash("old_doc", 50.0, "DOC")

    val newDrive = EventProcessor.applyEvent(initialDrive, event).toOption.get

    // Проверяем, что PDF остался, а DOC ушел в корзину
    assert(newDrive.activeFiles.contains(fileToKeep))
    assert(!newDrive.activeFiles.contains(fileToTrash))
    assert(newDrive.trashedFiles.contains(fileToTrash))
  }

  test("Processor: Apply EMPTY_TRASH successfully") {
    val file = CloudFile("junk", 50.0, "doc")
    val initialDrive = CloudDrive("user1", 500.0, trashedFiles = List(file))

    val newDrive = EventProcessor.applyEvent(initialDrive, EmptyTrash).toOption.get
    assert(newDrive.trashedFiles.isEmpty)
  }

  test("Processor: Apply PROMO_SPACE successfully") {
    val initialDrive = CloudDrive("user1", 100.0)

    val newDrive = EventProcessor.applyEvent(initialDrive, ApplyPromo(500.0)).toOption.get
    assert(CloudLogic.totalCapacity(newDrive) == 600.0)
  }
}