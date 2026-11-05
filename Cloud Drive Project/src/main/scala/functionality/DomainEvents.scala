package functionality

// 1. Domain Events
sealed trait DriveEvent
case class Upload(fileName: String, sizeMb: Double, fileType: String) extends DriveEvent
case class MoveToTrash(fileName: String, sizeMb: Double, fileType: String) extends DriveEvent
case object EmptyTrash extends DriveEvent
case class ApplyPromo(bonusMb: Double) extends DriveEvent

// 2. Errors
sealed trait ParseError
case class MalformedRow(row: String, reason: String) extends ParseError

sealed trait BusinessError
case class InsufficientSpace(fileName: String, required: Double, free: Double) extends BusinessError