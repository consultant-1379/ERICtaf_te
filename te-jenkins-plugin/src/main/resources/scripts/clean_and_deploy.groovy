import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

def timestamp = new Date()
def date = timestamp.format("yyyy-MM-dd")
def time = timestamp.format("HH:mm:ss")
println("/opt/project/${scheduleName}/${date}/${time}/${name}");

zipDir("c:/Temp/ve-server", "c:/Temp/stuff2.zip");

private static void zipDir(String dir, String zipFileName) throws Exception {
    File dirObj = new File(dir);
    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
    println("Creating : " + zipFileName);
    addDir(dir, dirObj, out);
    out.close();
}

static void addDir(String root, File dirObj, ZipOutputStream out) throws IOException {
    File[] files = dirObj.listFiles();
    byte[] tmpBuf = new byte[1024];

    for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
            addDir(root, files[i], out);
            continue;
        }
        FileInputStream stream = new FileInputStream(files[i].getAbsolutePath());
        def fileName = files[i].getAbsolutePath().substring(root.length() + 1)
        println(" Adding: " + fileName);
        out.putNextEntry(new ZipEntry(fileName));
        int len;
        while ((len = stream.read(tmpBuf)) > 0) {
            out.write(tmpBuf, 0, len);
        }
        out.closeEntry();
        stream.close();
    }
}
