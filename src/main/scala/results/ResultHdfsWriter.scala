package results

import hactors.ResultCollector.DeckResult
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

class ResultHdfsWriter(results: List[DeckResult]){
  // is it needed?
  def write(uri: String, filePath: String) = {
    System.setProperty("HADOOP_USER_NAME", "volodymyr")

    val path = new Path(filePath)
    val conf = new Configuration()
    conf.set("fs.defaultFS", uri)
    val fs = FileSystem.get(conf)
    val os = fs.create(path)
    val data = "hello world".getBytes
    os.write(data)
    fs.close()
  }

}