package teaforge.platform.httpserver.types

import org.http4k.core.Body
import org.http4k.format.Jackson.auto

data class Challenge(val title: String, val points: Int)

data class ChallengeListResponse(val challenges: List<Challenge>)

val challengeListResponseLens = Body.auto<ChallengeListResponse>().toLens()
