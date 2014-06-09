/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.config;

import de.charite.compbio.exomiser.util.ExomiserOptionsCommandLineParser;
import de.charite.compbio.exomiser.util.OutputFormat;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for setting-up the command-line options. If you want a 
 * new option on the command line, add it here.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
public class CommandLineOptionsConfig {

    @Bean
    public ExomiserOptionsCommandLineParser exomiserCommandLineOptionsParser() {
        return new ExomiserOptionsCommandLineParser(options());
    }
                      
    @Bean
    protected Options options() {
        Options options = new Options();

        addHelpOptions(options);
        addSampleDataOptions(options);
        addFilterOptions(options);
        addPrioritiserOptions(options);
        addOutputOptions(options);

        return options;
    }

    private void addHelpOptions(Options options) {
        options.addOption(new Option("h", "help", false, "Shows this help"));
        options.addOption(new Option("H", "help", false, "Shows this help"));
    }

    private void addSampleDataOptions(Options options) {
        //input files - at least the VCF file is required!
        Option inputVcf = OptionBuilder
                .withArgName("file")
                .isRequired()
                .hasArg()
                .withDescription("Path to VCF file with mutations to be analyzed. Can be either for an individual or a family.")
                .withLongOpt("vcf")
                .create("v");
        options.addOption(inputVcf);

        options.addOption(OptionBuilder
                .withArgName("file")
                .hasArg()
                .withDescription("Path to pedigree (ped) file. Required if the vcf file is for a family.")
                .withLongOpt("ped")
                .create("p")
        );
    }

    private void addFilterOptions(Options options) {
        // Filtering options
        //Do filters filter-out or retain the options specified below? Would be good to spell this out in all cases.
        options.addOption(new Option("F", "max-freq", true, "Maximum frequency threshold for variants to be retained. e.g. 100.00 will retain all variants. Default: 100.00")); // FrequencyFilter filter above or below threshold?
        options.addOption(new Option("R", "restrict-interval", true, "Restrict to region/interval (e.g., chr2:12345-67890)")); //IntervalFilter
        options.addOption(new Option("Q", "min-qual", true, "Mimimum quality threshold for variants as specifed in VCF 'QUAL' column.  Default: 0")); //QualityFilter
        //no extra args required - these are Booleans 
        options.addOption(new Option("P", "include-pathogenic", false, "Filter variants to include those with predicted pathogenicity. Default: false"));//PathogenicityFilter 
        options.addOption(new Option(null, "remove-dbsnp", false, "Filter out all variants with an entry in dbSNP/ESP (regardless of frequency).  Default: false"));
        //TODO: WTF is going on with PathogenicityFilter? It actualy needs boolean filterOutNonpathogenic, boolean removeSynonomousVariants
        //but these set (or don't set) things in the PathogenicityTriage - maybe we could have a MissensePathogenicityFilter too? 
        options.addOption(new Option("O", "exclude-pathogenic-missense", false, "Filter variants to include those with predicted pathogenicity - MISSENSE MUTATIONS ONLY"));//PathogenicityFilter 
        options.addOption(new Option("T", "remove-off-target-syn", false, "Keep off-target variants. These are defined as intergenic, intronic, upstream, downstream, synonymous or intronic ncRNA variants. Default: true")); //TargetFilter 
    }

    private void addPrioritiserOptions(Options options) {
        // Prioritiser options - may or may not be required depending on the priotitiser chosen.
        options.addOption(new Option(null, "candidate-gene", true, "Known or suspected gene association e.g. FGFR2"));
        options.addOption(new Option(null, "hpo-ids", true, "Comma separated list of HPO IDs for the sample being sequenced e.g. HP:0000407,HP:0009830,HP:0002858"));
        options.addOption(new Option("S", "seed-genes", true, "Comma separated list of seed genes (Entrez gene IDs) for random walk"));
        //Prioritisers - Apart from the disease and inheritance prioritisers are all mutually exclusive.
        options.addOption(new Option("D", "disease-id", true, "OMIM ID for disease being sequenced. e.g. OMIM:101600")); //OMIMPriority
        options.addOption(new Option("I", "inheritance-mode", true, "Filter variants for inheritance pattern (AR, AD, X)")); //InheritancePriority change to DOMINANT / RECESSIVE / X ? Inclusive or exclusive?
        //The desired prioritiser e.g. --prioritiser=pheno-wanderer or --prioritiser=zfin-phenodigm
        //this is less ambiguous to the user and makes for easier parsing. Can then check that all the required fields are present before proceeding.
        Option priorityOption = OptionBuilder
                .isRequired()
                .hasArg()
                .withArgName("name")
                .withValueSeparator()
                .withDescription("Name of the prioritiser used to score the genes. Can be one of: boqua, dynamic-pheno-wanderer, gene-wanderer, pheno-wanderer, phenomizer, uber-pheno, mgi-phenodigm or zfin-phenodigm. e.g. --prioritiser=mgi-phenodigm")
                .withLongOpt("prioritiser")
                .create();
        options.addOption(priorityOption);
    }

    private void addOutputOptions(Options options) {
        //output options
        options.addOption(new Option(null, "num-genes", true, "Number of genes to show in output"));
        options.addOption(new Option("o", "out-file", true, "name of out file (default: \"exomizer.html\")"));
        options.addOption(OptionBuilder
                .hasArg()
                .withArgName("type")
                .withType(OutputFormat.class)
                .withValueSeparator()
                .withDescription("Specify format option HTML, VCF or TAB e.g. --out-format=TAB")
                .withLongOpt("out-format")
                .create("f"));

        //TODO: check what this actually does (I think this is for Peter's CRE server, in which case it's not wanted here )
        options.addOption(new Option(null, "withinFirewall", false, "Set flag that we are running on private server"));
    }

        //the original options:
//        options.addOption(new Option("h", "help", false, "Shows this help"));
//        options.addOption(new Option("H", "help", false, "Shows this help"));
//        options.addOption(new Option("v", "vcf", true, "Path to VCF file with mutations to be analyzed."));
//        options.addOption(new Option("o", "outfile", true, "name of out file (default: \"exomizer.html\")"));
//        options.addOption(new Option("l", "log", true, "Configuration file for logger"));
//        // / Filtering options
//        options.addOption(new Option("A", "omim_disease", true, "OMIM ID for disease being sequenced"));
//        options.addOption(new Option("B", "boqa", true, "comma-separated list of HPO terms for BOQA"));
//        options.addOption(new Option("D", "file_for_deserialising", true, "De-serialise"));
//        options.addOption(new Option("F", "freq_threshold", true, "Frequency threshold for variants"));
//        options.addOption(new Option("I", "inheritance", true, "Filter variants for inheritance pattern (AR,AD,X)"));
//        options.addOption(new Option("M", "mgi_phenotypes", false, "Filter variants for MGI phenodigm score"));
//
//        options.addOption(new Option("P", "path", false, "Filter variants for predicted pathogenicity"));
//        options.addOption(new Option("Q", "qual_threshold", true, "Quality threshold for variants"));
//        options.addOption(new Option("S", "SeedGenes", true, "Comma separated list of seed genes for random walk"));
//        options.addOption(new Option("W", "RWmatrix", true, "Random walk matrix file"));
//        options.addOption(new Option("X", "RWindex", true, "Random walk index file"));
//        options.addOption(new Option("Z", "zfin_phenotypes", false, "Filter variants for ZFIN phenodigm score"));
//
//        // Annotations that do not filter
//        options.addOption(new Option(null, "interval", true, "Restrict to interval (e.g., chr2:12345-67890)"));
//        options.addOption(new Option(null, "tsv", false, "Output tab-separated value (TSV) file instead of HTML"));
//        options.addOption(new Option(null, "vcf_output", false, "Output VCF file instead of HTML"));
//        options.addOption(new Option(null, "candidate_gene", true, "Known or suspected gene association"));
//        options.addOption(new Option(null, "dbsnp", false, "Filter out all variants with an entry in dbSNP/ESP (regardless of frequency)"));
//        options.addOption(new Option(null, "ped", true, "pedigree (ped) file"));
//        options.addOption(new Option(null, "hpo", true, "HPO Ontology (obo) file"));
//        options.addOption(new Option(null, "hpoannot", true, "HPO Annotations file"));
//        options.addOption(new Option(null, "hpo_ids", true, "HPO IDs for the sample being sequenced"));
//        options.addOption(new Option(null, "ngenes", true, "Number of genes to show in output"));
//        options.addOption(new Option(null, "withinFirewall", false, "Set flag that we are running on private server"));
//        options.addOption(new Option(null, "phenomizerData", true, "Phenomizer data directory"));
}
