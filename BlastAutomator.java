// Description: This Java program automates the process of submitting a DNA sequence to the NCBI BLAST web service,
import java.io.*;
import java.net.*;
import java.util.regex.*;

public class BlastAutomator {

    // Submits the BLAST job and returns the RID (Request ID)
    public static String submitBlastJob(String sequence) throws Exception {
        String data = "CMD=Put&PROGRAM=blastn&DATABASE=nt&QUERY=" + URLEncoder.encode(sequence, "UTF-8");
        URL url = new URL("https://blast.ncbi.nlm.nih.gov/Blast.cgi");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(data.getBytes());
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }
        }

        // Extract RID from response
        Pattern ridPattern = Pattern.compile("RID = (\\S+)");
        Matcher matcher = ridPattern.matcher(response.toString());
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new RuntimeException("RID not found in response.");
        }
    }

    // Waits and then retrieves the result using the RID
    public static String getBlastResults(String rid) throws Exception {
        Thread.sleep(15000); // wait for 15 seconds before fetching result

        String reportUrl = "https://blast.ncbi.nlm.nih.gov/Blast.cgi?CMD=Get&FORMAT_TYPE=Text&RID=" + rid;
        URL url = new URL(reportUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }
        }

        return response.toString();
    }

    // Extracts identity percentage from the BLAST output
    public static void parseBlastOutput(String report) {
        Pattern identityPattern = Pattern.compile("Identities = (\\d+/\\d+) \\((\\d+)%\\)");
        Matcher matcher = identityPattern.matcher(report);

        if (matcher.find()) {
            System.out.println("Alignment: " + matcher.group(1));
            System.out.println("Percent Identity: " + matcher.group(2) + "%");
        } else {
            System.out.println("No alignment found.");
        }
    }

    public static void main(String[] args) throws Exception {
        //custom DNA sequence
        String originalSeq = "GTAGGTCTTTGGCATTAGGAGCTTGAGCCCAGACGGCCCTAGCAGGGACCCCAGCGCCCGAGAGACC"
                + "ATGCAGAGGTCGCCTCTGGAAAAGGCCAGCGTTGTCTCCAAACTTTTTTTCAGGTGAGAAGGTGGCCAAC"
                + "CGAGCTTCGGAAAGACACGTGCCCACGAAAGAGGAGGGCGTGTGTATGGGTTGGGTTTGGGGTAAAGGAA"
                + "TAAGCAGTTTTTAAAAAGATGCGCTATCATTCATTGTTTTGAAAGAAAATGTGGGTATTGTAGAATAAAA"
                + "CAGAAAGCATTAAGAAGAGATGGAAGAATGAACTGAAGCTGATTGAATAGAGAGCCACATCTACTTGCAA"
                + "CTGAAAAGTTAGAATCTCAAGACTCAAGTACGCTACTATGCACTTGTTTTATTTCATTTTTCTAAGAAAC"
                + "TAAAAATACTTGTTAATAAGTACCTAAGTATGGTTTATTGGTTTTCCCCCTTCATGCCTTGGACACTTGA"
                + "TTGTCTTCTTGGCACATACAGGTGCCATGCCTGCATATAGTAAGTGCTCAG";

        System.out.println("Submitting BLAST job...");
        String rid = submitBlastJob(originalSeq);

        System.out.println("Waiting for results...");
        String report = getBlastResults(rid);

        System.out.println("Parsing result...");
        parseBlastOutput(report);
    }
}


    
