import org.scalatest.funsuite.AnyFunSuite
import functionality.{CsvParser, Upload, MoveToTrash, EmptyTrash, ApplyPromo, MalformedRow}

class CsvParserSpec extends AnyFunSuite {

  test("Parser: Valid UPLOAD row") {
    val row = "UPLOAD,photo1,250.5,IMG,"
    assert(CsvParser.parseRow(row) == Right(Upload("photo1", 250.5, "IMG")))
  }

  test("Parser: Valid TRASH row (now with fileType)") {
    // Теперь колонка p3 (DOC) тоже считывается парсером для удаления
    val row = "TRASH,doc_final,15.0,DOC,"
    assert(CsvParser.parseRow(row) == Right(MoveToTrash("doc_final", 15.0, "DOC")))
  }

  test("Parser: Valid EMPTY_TRASH row") {
    val row = "EMPTY_TRASH,,,,"
    assert(CsvParser.parseRow(row) == Right(EmptyTrash))
  }

  test("Parser: Valid PROMO_SPACE row") {
    val row = "PROMO_SPACE,1024.0,,,"
    assert(CsvParser.parseRow(row) == Right(ApplyPromo(1024.0)))
  }

  test("Parser: Invalid row - Missing columns") {
    val row = "UPLOAD,photo1" // Не хватает запятых
    assert(CsvParser.parseRow(row) == Left(MalformedRow(row, "Not enough columns")))
  }

  test("Parser: Invalid row - Unknown event") {
    val row = "DOWNLOAD,file1,10.0,,"
    assert(CsvParser.parseRow(row) == Left(MalformedRow(row, "Unknown event code: DOWNLOAD")))
  }

  test("Parser: Invalid TRASH - Wrong file type (Enum)") {
    // Ожидаем ошибку, если тип файла для удаления не соответствует разрешенным (например, EXE)
    val row = "TRASH,virus,50.0,EXE,"
    assert(CsvParser.parseRow(row) == Left(MalformedRow(row, "Invalid file type: EXE")))
  }
}