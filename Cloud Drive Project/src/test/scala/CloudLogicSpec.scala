import org.scalatest.funsuite.AnyFunSuite
import entities.{CloudDrive, CloudFile}
import functionality.CloudLogic.*

class CloudLogicSpec extends AnyFunSuite {

  test("Upload file: successful when there is enough free space") {
    val drive = CloudDrive("user-1", 100.0)
    val file = CloudFile("photo", 30.0, " JPG ")

    info(s"Attempting to upload file: '${file.name}.${file.fileType.trim.toLowerCase}'")
    info(s"File size: ${file.sizeMb} MB | Current free space on drive: ${freeSpace(drive)} MB")

    val result = uploadFile(drive, file)

    assert(result.isRight, "Upload should be successful")
    result.foreach { updatedDrive =>
      info(s"SUCCESS! File uploaded. Remaining free space: ${freeSpace(updatedDrive)} MB")

      val uploadedFile = updatedDrive.activeFiles.head
      assert(uploadedFile.fileType == "jpg")
      assert(uploadedFile.name == "photo")
      assert(freeSpace(updatedDrive) == 70.0)
    }
  }

  test("Upload file: rejected when file size exceeds free space") {
    val drive = CloudDrive("user-1", 50.0)
    val file = CloudFile("video", 60.0, "mp4")

    info(s"Attempting to upload file: '${file.name}.${file.fileType}'")
    info(s"File size: ${file.sizeMb} MB | Current free space on drive: ${freeSpace(drive)} MB")

    val result = uploadFile(drive, file)

    assert(result.isLeft, "Upload should be rejected")
    result.left.foreach { errorMsg =>
      info(s"REJECTED! Reason: $errorMsg")
    }

    assert(result.left.toOption.get.contains("Not enough space"))
  }

  test("Move to trash: frees up active space and moves file to trashed") {
    val file = CloudFile("doc", 20.0, "pdf")
    val drive = CloudDrive("user-1", 100.0, activeFiles = List(file))

    info(s"Initial state: Drive has 1 active file '${file.name}.${file.fileType}' (${file.sizeMb} MB)")
    info(s"Current free space: ${freeSpace(drive)} MB")

    val updated = moveToTrash(drive, file)

    info(s"Action: Moved '${file.name}.${file.fileType}' to trash")
    info(s"Result: Active files = ${updated.activeFiles.size}, Trashed files = ${updated.trashedFiles.size}")
    info(s"New free space: ${freeSpace(updated)} MB")

    assert(updated.activeFiles.isEmpty)
    assert(updated.trashedFiles.contains(file))
    assert(freeSpace(updated) == 100.0, "Free space should be restored after moving to trash")
  }

  test("Empty trash: permanently deletes trashed files") {
    val file = CloudFile("junk", 15.0, "tmp")
    val drive = CloudDrive("user-1", 100.0, trashedFiles = List(file))

    info(s"Initial state: Trash contains '${file.name}.${file.fileType}' (${file.sizeMb} MB)")

    val updated = emptyTrash(drive)

    info("Action: Emptied the trash")
    info(s"Result: Trashed files count = ${updated.trashedFiles.size}")

    assert(updated.trashedFiles.isEmpty)
  }

  test("Apply promo: increases total and free capacity") {
    val drive = CloudDrive("user-1", 100.0)
    val bonus = 50.0

    info(s"Initial state: Total capacity = ${totalCapacity(drive)} MB")
    info(s"Action: Applying promo code for $bonus MB")

    val updated = applyPromo(drive, bonus)

    info(s"Result: New total capacity = ${totalCapacity(updated)} MB, New free space = ${freeSpace(updated)} MB")

    assert(totalCapacity(updated) == 150.0)
    assert(freeSpace(updated) == 150.0)
  }

  test("Analytics: correctly calculates trash space percentage and finds largest file") {
    val smallFile = CloudFile("small", 10.0, "txt")
    val bigFile = CloudFile("movie", 40.0, "mkv")

    val drive = CloudDrive(
      Id = "user-1",
      baseCapacity = 100.0,
      activeFiles = List(smallFile),
      trashedFiles = List(bigFile)
    )

    info(s"Drive state: Total capacity = 100.0 MB")
    info(s"Active files: '${smallFile.name}' (${smallFile.sizeMb} MB)")
    info(s"Trashed files: '${bigFile.name}' (${bigFile.sizeMb} MB)")

    val trashPercent = trashSpacePercent(drive)
    info(s"Analytics 1: Trash space percentage calculated as ${trashPercent * 100}%")

    val largest = largestFile(drive)
    largest.foreach { f =>
      info(s"Analytics 2: Largest file found is '${f.name}.${f.fileType}' (${f.sizeMb} MB)")
    }

    assert(trashPercent == 0.4)
    assert(largest.contains(bigFile))
  }
}