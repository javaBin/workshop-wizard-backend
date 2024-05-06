package no.javabin.repository

class AdminRepository {
/*
    fun getWorkshops(): List<AdminWorkshopDTO> {
        return workshopMap.map { workshop ->
            val registrations =
                registrationMap.filter { it.value.workshop.id == workshop.key }
                    .map { it.value }
                    .map {
                        AdminWorkshopRegistrationDTO(
                            userMap[it.user.id]!!.firstName,
                            userMap[it.user.id]!!.lastName,
                            userMap[it.user.id]!!.email,
                            it.state,
                        )
                    }
            AdminWorkshopDTO(workshop.value.title, workshop.value.teacherName, registrations)
        }
    }

    fun getWorkshopRegistrations(workshopId: Int): AdminWorkshopDTO {
        val workshop = workshopMap.get(workshopId) ?: throw RuntimeException("Workshop does not exist")
        val registrations =
            registrationMap.filter { it.value.workshop.id == workshop.id }
                .map { it.value }
                .map {
                    AdminWorkshopRegistrationDTO(
                        userMap[it.user.id]!!.firstName,
                        userMap[it.user.id]!!.lastName,
                        userMap[it.user.id]!!.email,
                        it.state,
                    )
                }
        return AdminWorkshopDTO(workshop.title, workshop.teacherName, registrations)
    }
    */
}
