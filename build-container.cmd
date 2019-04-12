mvn compile com.google.cloud.tools:jib-maven-plugin:1.0.2:dockerBuild ^
    -Djib.from.image=openjdk:jdk ^
    -Djib.to.image=liu233w/private-project:theoretical-evaluation ^
    -Djib.container.mainClass=edu.nwpu.machunyan.theoreticalEvaluation.application.ResolveTotInfoTestSuitSubset ^
    -Djib.container.workingDirectory=/workdir ^
    -Djib.container.args=-Dfile.encoding=UTF8 ^
    -Djib.httpTimeout=60000
