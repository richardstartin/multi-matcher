package io.github.richardstartin.multimatcher.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Check if aggregation of an array at an offset vectorises or not
 */
public class LogicalAggregationBenchmark {


    public static void main(String... args) {
        for (int i = 0; i < 256; ++i) {
            System.out.println("\"" + i + "\", ");
        }
    }

    @State(Scope.Benchmark)
    public static class BaseState {

        long[] gap;

        @Param("256")
        int targetSize;

        @Param("1024")
        int sourceSize;

        @Param({"0",
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "10",
                "11",
                "12",
                "13",
                "14",
                "15",
                "16",
                "17",
                "18",
                "19",
                "20",
                "21",
                "22",
                "23",
                "24",
                "25",
                "26",
                "27",
                "28",
                "29",
                "30",
                "31",
                "32",
                "33",
                "34",
                "35",
                "36",
                "37",
                "38",
                "39",
                "40",
                "41",
                "42",
                "43",
                "44",
                "45",
                "46",
                "47",
                "48",
                "49",
                "50",
                "51",
                "52",
                "53",
                "54",
                "55",
                "56",
                "57",
                "58",
                "59",
                "60",
                "61",
                "62",
                "63",
                "64",
                "65",
                "66",
                "67",
                "68",
                "69",
                "70",
                "71",
                "72",
                "73",
                "74",
                "75",
                "76",
                "77",
                "78",
                "79",
                "80",
                "81",
                "82",
                "83",
                "84",
                "85",
                "86",
                "87",
                "88",
                "89",
                "90",
                "91",
                "92",
                "93",
                "94",
                "95",
                "96",
                "97",
                "98",
                "99",
                "100",
                "101",
                "102",
                "103",
                "104",
                "105",
                "106",
                "107",
                "108",
                "109",
                "110",
                "111",
                "112",
                "113",
                "114",
                "115",
                "116",
                "117",
                "118",
                "119",
                "120",
                "121",
                "122",
                "123",
                "124",
                "125",
                "126",
                "127",
                "128",
                "129",
                "130",
                "131",
                "132",
                "133",
                "134",
                "135",
                "136",
                "137",
                "138",
                "139",
                "140",
                "141",
                "142",
                "143",
                "144",
                "145",
                "146",
                "147",
                "148",
                "149",
                "150",
                "151",
                "152",
                "153",
                "154",
                "155",
                "156",
                "157",
                "158",
                "159",
                "160",
                "161",
                "162",
                "163",
                "164",
                "165",
                "166",
                "167",
                "168",
                "169",
                "170",
                "171",
                "172",
                "173",
                "174",
                "175",
                "176",
                "177",
                "178",
                "179",
                "180",
                "181",
                "182",
                "183",
                "184",
                "185",
                "186",
                "187",
                "188",
                "189",
                "190",
                "191",
                "192",
                "193",
                "194",
                "195",
                "196",
                "197",
                "198",
                "199",
                "200",
                "201",
                "202",
                "203",
                "204",
                "205",
                "206",
                "207",
                "208",
                "209",
                "210",
                "211",
                "212",
                "213",
                "214",
                "215",
                "216",
                "217",
                "218",
                "219",
                "220",
                "221",
                "222",
                "223",
                "224",
                "225",
                "226",
                "227",
                "228",
                "229",
                "230",
                "231",
                "232",
                "233",
                "234",
                "235",
                "236",
                "237",
                "238",
                "239",
                "240",
                "241",
                "242",
                "243",
                "244",
                "245",
                "246",
                "247",
                "248",
                "249",
                "250",
                "251",
                "252",
                "253",
                "254",
                "255"})
        int padding;

        long[] source;
        long[] target;


        @Setup(Level.Trial)
        public void setup() {
            source = new long[sourceSize];
            gap = new long[padding];
            target = new long[targetSize];
            fill(source);
            fill(target);
        }

        private static void fill(long[] data) {
            for (int i = 0; i < data.length; ++i) {
                data[i] = ThreadLocalRandom.current().nextLong();
            }
        }

    }

    public static class ConstantOffset0State extends BaseState {

        private static final int offset = 0;
    }

    public static class ConstantOffset256State extends BaseState {

        private static final int offset = 256;
    }

    public static class ConstantOffset512State extends BaseState {

        private static final int offset = 512;
    }


    public static class DynamicOffsetState extends BaseState {
        @Param({"0", "256", "512", "768"})
        int offset;
    }


    @Benchmark
    public void intersectionWithOffset(DynamicOffsetState state, Blackhole bh) {
        var target = state.target;
        var source = state.source;
        int offset = state.offset;
        for (int i = 0; i < state.target.length; ++i) {
            target[i] &= source[offset + i];
        }
        bh.consume(target);
    }


    @Benchmark
    public void intersectionWithConstantOffset256(BaseState state, Blackhole bh) {
        var target = state.target;
        var source = state.source;
        for (int i = 0; i < state.target.length; ++i) {
            target[i] &= source[ConstantOffset256State.offset + i];
        }
        bh.consume(target);
    }

    @Benchmark
    public void intersectionWithConstantOffset0(BaseState state, Blackhole bh) {
        var target = state.target;
        var source = state.source;
        for (int i = 0; i < state.target.length; ++i) {
            target[i] &= source[ConstantOffset0State.offset + i];
        }
        bh.consume(target);
    }



    @Benchmark
    public void intersectionWithConstantOffset512(BaseState state, Blackhole bh) {
        var target = state.target;
        var source = state.source;
        for (int i = 0; i < state.target.length; ++i) {
            target[i] &= source[ConstantOffset512State.offset + i];
        }
        bh.consume(target);
    }



    @Benchmark
    public void intersectionNoOffset(BaseState state, Blackhole bh) {
        var target = state.target;
        var source = state.source;
        for (int i = 0; i < state.target.length; ++i) {
            target[i] &= source[i];
        }
        bh.consume(target);
    }
}
