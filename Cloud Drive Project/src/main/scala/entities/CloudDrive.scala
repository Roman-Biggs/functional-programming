package entities

final case class CloudDrive (
  Id: String,
  baseCapacity: Double,
  promoCapacity: Double = 0.0,
  activeFiles: List[CloudFile] = Nil,
  trashedFiles: List[CloudFile] = Nil
)
