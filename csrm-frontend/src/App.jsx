import { useState } from "react";
import Login from "./Login.jsx";
import Register from "./Register.jsx";
import Booking from "./Booking.jsx";
import Admin from "./Admin.jsx";

export default function App() {
  const [user, setUser] = useState(JSON.parse(localStorage.getItem("user") || "null"));
  const [page, setPage] = useState("login");

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setUser(null);
    setPage("login");
  };

  if (!user) {
    return page === "login"
      ? <Login onLogin={setUser} goRegister={() => setPage("register")} />
      : <Register goLogin={() => setPage("login")} />;
  }

  return (
    <div>
      <h2>Campus Resource Management</h2>
      <p>
        Logged in as <b>{user.username}</b> ({user.role}){" "}
        <button onClick={logout}>Logout</button>
      </p>
      <hr />
      {user.role === "ADMIN" ? <Admin /> : <Booking user={user} />}
    </div>
  );
}
