pbjs -t static-module -w closure -r osmpbf -o ../../src/assets/js/osmpbf.js ../proto/fileformat.proto ../proto/osmformat.proto
pbts -o ../../src/types/osmpbf.d.ts ../../src/assets/js/osmpbf.js