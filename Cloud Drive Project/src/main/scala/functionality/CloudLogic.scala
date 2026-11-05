package functionality

import entities.{CloudDrive, CloudFile}

object CloudLogic {

  // Basic logic
  def totalCapacity(drive: CloudDrive): Double =
    drive.baseCapacity + drive.promoCapacity

  def activeSpace(drive: CloudDrive): Double =
    drive.activeSpace

  def trashSpace(drive: CloudDrive): Double =
    drive.trashSpace

  def freeSpace(drive: CloudDrive): Double =
    totalCapacity(drive) - activeSpace(drive)

  // Status update actions

  def uploadFile(drive: CloudDrive, file: CloudFile): Either[String, CloudDrive] = {
    // Зберігаємо відформатовану копію у нове значення
    val formattedFile = file.copy(fileType = file.fileType.trim().toLowerCase())

    if formattedFile.sizeMb > freeSpace(drive) then
      Left(s"Not enough space for file: ${formattedFile.name}.${formattedFile.fileType}. Free space: ${freeSpace(drive)} MB.")
    else
      Right(
        drive.copy(
          activeFiles = formattedFile :: drive.activeFiles,
          activeSpace = drive.activeSpace + formattedFile.sizeMb
        )
      )
  }

  def moveToTrash(drive: CloudDrive, file: CloudFile): CloudDrive = {
    // Partition method splits list into cortege of two lists by condition(-s)
    val (toTrash, remainingActive) = drive.activeFiles.partition(x => x.name == file.name && x.fileType == file.fileType.trim().toLowerCase())
    val movedSize = toTrash.map(_.sizeMb).sum

    drive.copy(
      activeFiles = remainingActive,
      trashedFiles = toTrash ::: drive.trashedFiles,
      activeSpace = drive.activeSpace - movedSize,
      trashSpace = drive.trashSpace + movedSize
    )
  }

  def emptyTrash(drive: CloudDrive): CloudDrive =
    drive.copy(trashedFiles = Nil, trashSpace = 0.0)

  def applyPromo(drive: CloudDrive, bonusMb: Double): CloudDrive =
    if bonusMb <= 0 then drive
    else drive.copy(promoCapacity = drive.promoCapacity + bonusMb)

  // Analytics

  def trashSpacePercent(drive: CloudDrive): Double =
    val capacity = totalCapacity(drive)
    if capacity <= 0 then 0.0 else trashSpace(drive) / capacity

  def largestFile(drive: CloudDrive): Option[CloudFile] =
    (drive.activeFiles ++ drive.trashedFiles).maxByOption(_.sizeMb) // ++ list concatenation operator (universal)

}
