import mpi.MPI
import java.io.File
import java.io.FileOutputStream

val MAX_LONG = 9223372036854775807

fun main(args: Array<String>) {

    MPI.Init(args)

    if (MPI.COMM_WORLD.Rank() == 0) {
        val (inputFilename, outputFilename) = args.takeLast(n = 2)
        val numberOfProcesses = MPI.COMM_WORLD.Size()
        val input = File(inputFilename).useLines { it.toList() }
        val size = input[0].toInt()

        val dist = Array(size) { Array(size) { MAX_LONG } }

        for (i in 1 until input.size) {
            val (id1, id2, weight) = input[i].split(' ').map(String::toInt)
            dist[id1][id2] = weight.toLong()
            dist[id2][id1] = weight.toLong()
        }

        for (i in 0 until size) {
            dist[i][i] = 0L
        }



        for (id in 1 until numberOfProcesses){
            val start = id * (size / numberOfProcesses)
            var end = size
            if (id != numberOfProcesses-1){
                end = (id + 1) * (size / numberOfProcesses)
            } // for example, if we have 5 vertices and 4 processes, they would be assigned as such: 1; 2; 3; 4, 5


        }
    }
}






@Volatile
var dist = emptyArray<Array<Long>>()
@Volatile
var kString = emptyArray<Long>()
@Volatile
var kColumn = emptyArray<Long>()

fun mainn(args: Array<String>) {

    MPI.Init(args)

    val (inputFilename, outputFilename) = args.takeLast(n = 2)
    val numberOfProcesses = args[args.size - 4].toInt()
    val input = File(inputFilename).useLines { it.toList() }
    val size = input[0].toInt()

    if (MPI.COMM_WORLD.Rank() == 0) {
        dist = Array(size) { Array(size) { MAX_LONG } }

        for (i in 1 until input.size) {
            val (id1, id2, weight) = input[i].split(' ').map(String::toInt)
            dist[id1][id2] = weight.toLong()
            dist[id2][id1] = weight.toLong()
        }

        for (i in 0 until size) {
            dist[i][i] = 0L
        }
    }

//    if (MPI.COMM_WORLD.Rank() != 0) {
//        println("fff")
//        for (i in dist){
//            for (j in i){
//                print(i)
//            }
//            println()
//        }
//    }

    val id = MPI.COMM_WORLD.Rank()
    val start = id * (size / numberOfProcesses)
    var end = size
    if (id != numberOfProcesses-1){
        end = (id + 1) * (size / numberOfProcesses)
    }

    for (k in 0 until size) {

        if (k in start until end) {
            kString = dist[k]
            kColumn = Array(size) { 0 }
        }

        for (i in start until end) {
            println("$id, $i, $k")
            kColumn[i] = dist[i][k]
        }

        for (i in start until end) {
            for (j in 0 until size) {
                if (kColumn[i] + kString[j] > 0L && kColumn[i] + kString[j] < dist[i][j]) {
                    dist[i][j] = kColumn[i] + kString[j] // if I do it with min it doesn't work correctly
                    // because Long overflows into negative numbers
                }
            }
        }


    }

    if (MPI.COMM_WORLD.Rank() == 0) {
        val outputStream = FileOutputStream(outputFilename)

        for (i in 0 until size) {
            for (j in 0 until size) {
                outputStream.write(dist[i][j].toString().toByteArray())
                outputStream.write(" ".toByteArray())
            }
            outputStream.write("\n".toByteArray())
        }

        outputStream.close()
    }
    MPI.Finalize()
}
