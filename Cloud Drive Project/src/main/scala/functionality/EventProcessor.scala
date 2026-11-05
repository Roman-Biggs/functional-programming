package functionality

import entities.{CloudDrive, CloudFile}

object EventProcessor {
  def applyEvent(drive: CloudDrive, event: DriveEvent): Either[BusinessError, CloudDrive] = event match {
    
    case Upload(name, sizeMb, fileType) =>
      val file = CloudFile(name, sizeMb, fileType)
      // .left.map block is ignored if uploadFile function returns Right (so when freeSpace is enough)
      CloudLogic.uploadFile(drive, file).left.map(msg => InsufficientSpace(name, sizeMb, CloudLogic.freeSpace(drive)))
      
    case MoveToTrash(name, sizeMb, fileType) => // sizeMb is technically ignored in logic, it uses the existing file's size
      val fileToTrash = CloudFile(name, sizeMb, fileType) // You might need to adjust logic if type is needed for trash
      Right(CloudLogic.moveToTrash(drive, fileToTrash))
    
    case EmptyTrash => Right(CloudLogic.emptyTrash(drive))
    
    case ApplyPromo(bonus) => Right(CloudLogic.applyPromo(drive, bonus))
  }
}