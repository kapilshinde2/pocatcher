import { QueryClientProvider } from "@tanstack/react-query"
import { queryClient } from "./api/query-client"
import { Route } from "react-router"
import { Routes } from "react-router"
import Home from "./features/home/components/Home"

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Routes>
        <Route path="/" element={<Home />} />
      </Routes>
    </QueryClientProvider>
  )
}

export default App
