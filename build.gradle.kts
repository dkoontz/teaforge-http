plugins {
    kotlin("jvm") version "2.1.0"
    `maven-publish`
    id("com.github.breadmoirai.github-release") version "2.5.2"
    `application` // TODO: remove this when publishing as a lib
}

//==== TODO: Remove this when publishing as a lib ====
application {
    mainClass = "teaforge.platform.httpserver.MainKt"
}
//==== TODO: Remove this when publishing as a lib ====

project.group = "io.github.dkoontz"

project.version = "0.1.0"

repositories { mavenCentral() }

dependencies {
    implementation(kotlin("stdlib"))
    implementation(platform("org.http4k:http4k-bom:6.14.0.0"))
    implementation("org.http4k.pro:http4k-tools-hotreload")
    implementation("org.http4k:http4k-api-openapi")
    implementation("org.http4k:http4k-client-okhttp")
    implementation("org.http4k:http4k-config")
    implementation("org.http4k:http4k-connect-storage-jdbc")
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-format-argo")
    implementation("org.http4k:http4k-format-dataframe")
    implementation("org.http4k:http4k-format-jackson")
    implementation("org.http4k:http4k-server-undertow")
    
    testImplementation(kotlin("test"))
    testImplementation("org.http4k:http4k-testing-approval")
    testImplementation("org.http4k:http4k-testing-hamkrest")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.13.0")
    testImplementation("org.junit.platform:junit-platform-launcher:1.13.1")
}

kotlin { jvmToolchain(21) }

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}

tasks.test { useJUnitPlatform() }

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            // Ensure dependencies are included in the published POM
            pom {
                withXml {
                    val dependenciesNode = asNode().appendNode("dependencies")
                    configurations.api.get().dependencies.forEach { dep ->
                        val dependencyNode = dependenciesNode.appendNode("dependency")
                        dependencyNode.appendNode("groupId", dep.group)
                        dependencyNode.appendNode("artifactId", dep.name)
                        dependencyNode.appendNode("version", dep.version)
                        dependencyNode.appendNode("scope", "compile")
                    }
                }
            }
        }
    }
}

githubRelease {
    token(System.getenv("GITHUB_TOKEN") ?: "")
    owner.set("dkoontz")
    repo.set(project.name)
    tagName.set("${project.version}")
    releaseName.set("Release ${project.version}")
    targetCommitish.set("main")
    body.set("Automated release for version ${project.version}")
    releaseAssets.setFrom(file("build/libs/${project.name}-${project.version}.jar"))
    draft.set(false)
    prerelease.set(false)
}

tasks.named("githubRelease") { dependsOn("jar") }

tasks.named("publish") { dependsOn("compileKotlin") }