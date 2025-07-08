package org.zenframework.z8.gradle.js

import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.zenframework.z8.gradle.base.ArtifactDependentTask

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors
import java.util.stream.IntStream

class EmbedSvgTask extends ArtifactDependentTask {

    @SkipWhenEmpty @InputDirectory final ConfigurableFileTree source = project.objects.fileTree()
    @OutputFile final RegularFileProperty output = project.objects.fileProperty()
    @Optional @Input final Property<String> prefix = project.objects.property(String.class)

    @TaskAction
    def run() {
        new FileWriter(this.output.asFile.get()).withCloseable { fw ->
            fw.write(this.prefix.getOrElse("""
[class*='${project.name}-icon-'] {
  width: 16px;
  height: 16px;
  background-repeat: no-repeat;
  background-size: contain;
  background-position: center;
}
"""))

            def db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            def transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")

            this.source.forEach { source ->
                def f = this.source.dir.toPath().relativize(source.toPath())
                if (f.getFileName().toString().endsWith(".svg")) {
                    def name = IntStream.range(0, f.nameCount)
                            .mapToObj { f.getName(it).toString() }
                            .collect(Collectors.joining("-"))
                        .with { it.substring(0, it.length() - 4) } // убрать .svg
                    fw.write(".${project.name}-icon-${name} {\n")
                    fw.write("    background-image: url('data:image/svg+xml,")

                    def document = db.parse(source)
                    document.normalizeDocument()
                    def writer = new StringWriter()
                    transformer.transform(new DOMSource(document), new StreamResult(writer))
                    def encoded = URLEncoder.encode(writer.toString(), StandardCharsets.UTF_8.toString())
                            .replaceAll("\\+", "%20")
                            .replaceAll("%21", "!")
                            .replaceAll("%27", "'")
                            .replaceAll("%28", "(")
                            .replaceAll("%29", ")")
                            .replaceAll("%7E", "~")
                    fw.write(encoded)
                    fw.write("');\n}\n\n")
                }
            }
            fw.flush()
        }
    }

}
