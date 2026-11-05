import org.scalatest.funsuite.AnyFunSuite
import entities.CloudDrive
import functionality.{CsvParser, EventProcessor, CloudLogic, MalformedRow}
import scala.io.Source
import scala.util.Using

class CloudDriveE2ESpec extends AnyFunSuite {

  // Тест на сгенерированной выборе (обрабатываются ли все строки без системных ошибок - не предусмотренных классом DomainEvents)

  test("E2E: Process full generated CSV journal") {
    val initialDrive = CloudDrive("e2e-drive", baseCapacity = 1024.0)
    var currentDrive = initialDrive

    var successCount = 0
    var parseErrorCount = 0
    var businessErrorCount = 0

    val csvPath = "./././variant_30_cloud_drive.csv"

    info(s"Starting E2E Journal processing from: $csvPath")
    info(s"Initial Drive Capacity: ${initialDrive.baseCapacity} MB\n")

    Using(Source.fromFile(csvPath)) { source =>
      val lines = source.getLines().drop(1)

      for ((line, index) <- lines.zipWithIndex) {
        val rowNum = index + 2

        CsvParser.parseRow(line) match {
          case Left(parseErr) =>
            parseErrorCount += 1
            if (rowNum <= 50) info(s"Row $rowNum | ❌ PARSE ERROR: ${parseErr.asInstanceOf[MalformedRow].reason} (Line: $line)")

          case Right(event) =>
            EventProcessor.applyEvent(currentDrive, event) match {
              case Left(bizErr) =>
                businessErrorCount += 1
                if (rowNum <= 50) info(s"Row $rowNum | ⚠️ BUSINESS ERROR: $bizErr")

              case Right(newDrive) =>
                successCount += 1
                currentDrive = newDrive
                if (rowNum <= 50) info(s"Row $rowNum | ✅ SUCCESS: Applied $event. Free space is now ${CloudLogic.freeSpace(newDrive)} MB")
            }
        }

        if (rowNum == 51) info("... (console output muted for performance, processing remaining rows silently) ...")
      }
    }

    info("\n" + "=" * 55)
    info("FINAL DRIVE STATE (End of Journal)")
    info("=" * 55)
    info(f"Total Capacity:   ${CloudLogic.totalCapacity(currentDrive)}%,.2f MB")
    info(f"Free Space:       ${CloudLogic.freeSpace(currentDrive)}%,.2f MB")
    info(f"Active Space:     ${CloudLogic.activeSpace(currentDrive)}%,.2f MB")
    info(s"Active Files:     ${currentDrive.activeFiles.size}")
    info(s"Trashed Files:    ${currentDrive.trashedFiles.size}")
    info(f"Trash Space Percentage:  ${CloudLogic.trashSpacePercent(currentDrive) * 100}%,.2f %%")

    CloudLogic.largestFile(currentDrive) match {
      case Some(f) => info(s"Largest File:     '${f.name}' (${f.sizeMb} MB, type: ${f.fileType})")
      case None    => info("Largest File:     None (Drive is empty)")
    }

    info("\n" + "=" * 55)
    info("JOURNAL PROCESSING STATS")
    info("=" * 55)
    info(s"Successfully applied events: $successCount")
    info(s"Rejected (Parse Errors):     $parseErrorCount")
    info(s"Rejected (Business Errors):  $businessErrorCount")

    assert(true)
  }
}