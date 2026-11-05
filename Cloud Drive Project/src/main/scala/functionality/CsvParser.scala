package functionality

import scala.util.Try

object CsvParser {
  def parseRow(line: String): Either[ParseError, DriveEvent] = {
    // split with -1 to keep empty trailing columns
    val cols = line.split(",", -1).map(_.trim)
    if (cols.length < 5) return Left(MalformedRow(line, "Not enough columns"))

    val eventCode = cols(0)
    val p1 = cols(1)
    val p2 = cols(2)
    val p3 = cols(3)

    eventCode match {
      case "UPLOAD" =>
        for {
          sizeMb <- parsePositiveDouble(p2, line)
          fileType <- parseFileType(p3, line)
          fileName = p1 // assuming any token is fine
        } yield Upload(fileName, sizeMb, fileType)

      case "TRASH" =>
        for {
          sizeMb <- parsePositiveDouble(p2, line)
          fileType <- parseFileType(p3, line)
          fileName = p1 // assuming any token is fine
        } yield MoveToTrash(fileName, sizeMb, fileType)

      case "EMPTY_TRASH" => Right(EmptyTrash)

      case "PROMO_SPACE" =>
        for {
          extraMb <- parsePositiveDouble(p1, line)
        } yield ApplyPromo(extraMb)

      case _ => Left(MalformedRow(line, s"Unknown event code: $eventCode"))
    }
  }

  private def parsePositiveDouble(s: String, row: String): Either[ParseError, Double] =
    Try(s.toDouble).toEither.left.map(_ => MalformedRow(row, s"Invalid number format: $s")).flatMap {
      case v if v > 0 => Right(v)
      case v => Left(MalformedRow(row, s"Number must be positive, got: $v"))
    }

  private def parseFileType(s: String, row: String): Either[ParseError, String] =
    s.toUpperCase match {
      case "DOC" | "IMG" | "VID" | "OTHER" => Right(s)
      case _ => Left(MalformedRow(row, s"Invalid file type: $s"))
    }
}
