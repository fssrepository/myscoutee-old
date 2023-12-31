package main

import (
	"fmt"
	"net/http"
)

func handleRequest(w http.ResponseWriter, r *http.Request) {
	// Read the request body
	//message := r.FormValue("message")

	// Echo the message back in the response
	fmt.Fprintf(w, "%s", "spring")
}

func main() {
	http.HandleFunc("/", handleRequest)

	port := 8080
	listenAddress := fmt.Sprintf(":%d", port)

	fmt.Printf("Echo server is listening on port %d\n", port)

	// Start the HTTP server
	err := http.ListenAndServe(listenAddress, nil)
	if err != nil {
		fmt.Println("Error starting the server:", err)
	}
}
