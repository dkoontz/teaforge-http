package teaforge.platform.httpserver

import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.DebuggingFilters.PrintRequest
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.jetbrains.kotlinx.dataframe.DataFrame
import teaforge.platform.httpserver.types.Challenge
import teaforge.platform.httpserver.types.ChallengeListResponse
import teaforge.platform.httpserver.types.JacksonMessage
import teaforge.platform.httpserver.types.challengeListResponseLens
import teaforge.platform.httpserver.types.dataframeLens
import teaforge.platform.httpserver.types.jacksonMessageLens

val app: HttpHandler =
        routes(
                // we want a wrapper function that validates request and response types and produces
                // appropriate 404, 302, etc
                //   - is this built-in to http4 somewhere?
                //   - if so can we get this without needing to implement an interface, etc.
                // do we want to do auth based on a token that gets set when visitin the QR "login"
                // page?
                //   - investigate a filter that implements auth based on a token
                "/ping" bind GET to { Response(OK).body("pong") },
                "/v1/challenges" bind GET to ::challenges,
                "/formats/dataframe" bind
                        POST to
                        {
                                val dataframe: DataFrame<*> = dataframeLens(it)
                                println(dataframe)
                                Response(OK)
                        },
                "/formats/json/jackson" bind
                        GET to
                        {
                                Response(OK)
                                        .with(
                                                jacksonMessageLens of
                                                        JacksonMessage("Barry", "Hello there!")
                                        )
                        },
        )

fun <TRequestType, TResponseType> validator(
        fn: (TRequestType) -> Pair<TResponseType, Status>
): ((String) -> Response) {

        return { request ->
                // attempt to decode TRequestType from request object
                //   otherwise return bad request error
                Response(OK).body("")
        }
}

fun challenges(req: Request): Response {
        return Response(OK)
                .with(
                        challengeListResponseLens of
                                ChallengeListResponse(
                                        listOf(
                                                Challenge("challenge1", 100),
                                                Challenge("challenge2", 200)
                                        )
                                )
                )
}

fun main() {
        val printingApp: HttpHandler = PrintRequest().then(app)

        val server = printingApp.asServer(Undertow(9000)).start()

        println("Server started on " + server.port())
}
