package edu.nwpu.machunyan.theoreticalEvaluation

import java.nio.file.Path
import java.nio.file.Paths


fun getTestFilePath(fileName: String): Path {
    return Paths.get(ClassLoader.getSystemResource(fileName).toURI())
}
