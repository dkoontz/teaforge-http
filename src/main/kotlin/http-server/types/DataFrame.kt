package teaforge.platform.httpserver.types

import org.http4k.core.Body
import org.http4k.format.dataframe.CSV
import org.http4k.format.dataframe.dataFrame

val dataframeLens = Body.dataFrame(CSV()).toLens()
