package no.javabin.config

import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import no.javabin.service.RegistrationWorkerService

fun Application.configureRegistrationWorker(registrationWorkerService: RegistrationWorkerService) {


    launch(Dispatchers.IO) {
        launch {
            log.info("Start background task [workshop database update, thread={}]", Thread.currentThread().threadId())
            while (isActive) { // isActive is true while the application is running
                try {
                    log.info(
                        "Start listening to tasks [workshop registration updates, thread={}]",
                        Thread.currentThread().threadId()
                    )
                    registrationWorkerService.handleMessages()
                    log.info("Finished task [workshop database update]")
                }catch(e: Exception){
                    log.error("Registration worker failed!\n${e.message}")
                }
            }
        }
    }
}

