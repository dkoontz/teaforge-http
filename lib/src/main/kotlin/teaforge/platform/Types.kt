package teaforge.platform.httpserver

import teaforge.ProgramConfig

typealias Program<TMessage, TModel> = 
    ProgramConfig<Effect, TMessage, TModel, Subscription>

// data class ProgramConfig<TEffect, TMessage, TModel, TSubscription>(
//         val init: (List<String>) -> Pair<TModel, List<TEffect>>,
//         val update: (TMessage, TModel) -> Pair<TModel, List<TEffect>>,
//         val subscriptions: (TModel) -> List<TSubscription>,
// )

// data class ProgramRunnerConfig<
//         TEffect, TMessage, TProgramModel, TRunnerModel, TSubscription, TSubscriptionState>(
//         val initRunner: (List<String>) -> TRunnerModel,
//         val processEffect: (TRunnerModel, TEffect) -> Pair<TRunnerModel, Maybe<TMessage>>,
//         val processSubscription:
//                 (TRunnerModel, TSubscriptionState) -> Triple<
//                                 TRunnerModel, TSubscriptionState, Maybe<TMessage>>,
//         val startSubscription:
//                 (TRunnerModel, TSubscription) -> Pair<TRunnerModel, TSubscriptionState>,
//         val stopSubscription: (TRunnerModel, TSubscriptionState) -> TRunnerModel,
//         val startOfUpdateCycle: (TRunnerModel) -> TRunnerModel,
//         val endOfUpdateCycle: (TRunnerModel) -> TRunnerModel,
//         val processHistoryEntry:
//                 (TRunnerModel, HistoryEntry<TMessage, TProgramModel>) -> TRunnerModel,
// )

sealed interface Effect<TMessage, TModel> {
    data class SendResponse(
        val id: RequestId,
        val headers : Headers
        val body : ResponseBody
    )

    data class RunTask<TMessage, TModel>(
        val task: Task<Effect<TMessage, TModel>,
    ) : Effect<TMessage, TModel>
}

sealed interface Task<T> {
    data class Completed<T>(
        val id: String,
        val result: T,
    ) : Task<T>

    data class InProgress<T>(
        val id: String,
        val nextStep: (T) -> Task<T>
    ) : Task<T>
}

// Teaforge ProgramRunnerConfig probably needs to have this added as a field
// so we have a platform defined way to step tasks forward.
// Somewhere the resolver has to track the state of the task, knowing what to 
// check to see if it is resolved. Is there a platform specific Task wrapper
// that is used for this purpose? Does there need to be a RoboRioTask, HttpServerTask, etc?

// Does that mean that .andThen works on type TPlatformTask<T> instead of Task<T>?
typealias TaskResolver<T> = (Task<T>) -> Task<T>


// fun sqlRequest(val query: String) : Task<DbResult> { ... }
// fun httpRequest(val url: String) : Task<HttpResult> { ... }

fun <StartValue,EndValue> Task<T,U>.andThen(
    nextStep: (StartValue) -> Task<EndValue>
): Task<EndValue> {
    Task.Step(this.id, this.resolver, nextStep)
}

fun <T,U> Task<T,U>.map(
    mapFunction: (T) -> U
): Task<U> {
    when (this) {
        is Task.Completed<T> -> Task.Completed(this.id, mapFunction(this.result))
        is Task.InProgress<T> -> 
            Task.InProgress(this.id, this.resolver, { result ->
                Task.InProgress(this.id, this.resolver, this.nextStep(mapFunction(result)))
            })
    }
}

sealed interface Subscription<TMessage, TModel> {
    data class HttpRequests<TMessage, TModel>(
        val id: RequestId
        val message: (HttpRequest) -> TMessage
    )
}

typealias Headers = List<String>
typealias RequestId = String
typealias RequestBody = String
typealias ResponseBody = String

data class HttpRequest<TMessage, TModel>(
    val url: String, // Create a custom type
    val headers: Headers
    val body: RequestBody,
)