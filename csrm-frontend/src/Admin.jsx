import { useEffect, useState } from "react";
import api, { errMsg } from "./api";

export default function Admin() {
  const [users, setUsers] = useState([]);
  const [resources, setResources] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [logs, setLogs] = useState([]);
  const [report, setReport] = useState([]);
  const [error, setError] = useState("");

  const [newRes, setNewRes] = useState({ name: "", type: "LAB", location: "" });
  const [logUser, setLogUser] = useState("");
  const [logDate, setLogDate] = useState("");
  const [repDate, setRepDate] = useState(new Date().toISOString().slice(0, 10));

  const run = async (fn) => {
    setError("");
    try { await fn(); } catch (e) { setError(errMsg(e)); }
  };

  const loadAll = () => run(async () => {
    setUsers((await api.get("/admin/users")).data);
    setResources((await api.get("/resources")).data);
    setBookings((await api.get("/bookings")).data);
  });

  useEffect(() => { loadAll(); }, []);

  const setStatus = (id, status) => run(async () => {
    await api.put(`/admin/users/${id}?status=${status}`);
    loadAll();
  });

  const addResource = () => run(async () => {
    await api.post("/resources", newRes);
    setNewRes({ name: "", type: "LAB", location: "" });
    loadAll();
  });

  const deleteResource = (id) => run(async () => {
    await api.delete("/resources/" + id);
    loadAll();
  });

  const cancelBooking = (id) => run(async () => {
    await api.delete("/bookings/" + id);
    loadAll();
  });

  const loadLogs = () => run(async () => {
    const params = {};
    if (logUser) params.userId = logUser;
    if (logDate) params.date = logDate;
    setLogs((await api.get("/audit", { params })).data);
  });

  const loadReport = () => run(async () => {
    setReport((await api.get("/admin/report", { params: { date: repDate } })).data);
  });

  const pending = users.filter((u) => u.status === "PENDING").length;
  const active = bookings.filter((b) => b.status === "BOOKED").length;

  return (
    <div>
      <h3>Dashboard</h3>
      <p>Resources: {resources.length} | Pending approvals: {pending} | Active bookings: {active}</p>
      <p className="err">{error}</p>

      <h3>Users</h3>
      <table>
        <thead><tr><th>ID</th><th>Username</th><th>Role</th><th>Status</th><th></th></tr></thead>
        <tbody>
          {users.map((u) => (
            <tr key={u.id}>
              <td>{u.id}</td><td>{u.username}</td><td>{u.role}</td><td>{u.status}</td>
              <td>
                {u.role !== "ADMIN" && (
                  <>
                    <button onClick={() => setStatus(u.id, "APPROVED")}>Approve</button>
                    <button onClick={() => setStatus(u.id, "REJECTED")}>Reject</button>
                  </>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <h3>Resources</h3>
      <input placeholder="Name" value={newRes.name} onChange={(e) => setNewRes({ ...newRes, name: e.target.value })} />
      <select value={newRes.type} onChange={(e) => setNewRes({ ...newRes, type: e.target.value })}>
        <option>CLASSROOM</option><option>LAB</option><option>LOCKER</option><option>EQUIPMENT</option>
      </select>
      <input placeholder="Location" value={newRes.location} onChange={(e) => setNewRes({ ...newRes, location: e.target.value })} />
      <button onClick={addResource}>Add</button>
      <table>
        <thead><tr><th>ID</th><th>Name</th><th>Type</th><th>Location</th><th></th></tr></thead>
        <tbody>
          {resources.map((r) => (
            <tr key={r.id}>
              <td>{r.id}</td><td>{r.name}</td><td>{r.type}</td><td>{r.location}</td>
              <td><button onClick={() => deleteResource(r.id)}>Delete</button></td>
            </tr>
          ))}
        </tbody>
      </table>

      <h3>All bookings</h3>
      <table>
        <thead><tr><th>ID</th><th>User ID</th><th>Resource ID</th><th>Start</th><th>End</th><th>Status</th><th></th></tr></thead>
        <tbody>
          {bookings.map((b) => (
            <tr key={b.id}>
              <td>{b.id}</td><td>{b.userId}</td><td>{b.resourceId}</td>
              <td>{b.startTime.replace("T", " ")}</td><td>{b.endTime.replace("T", " ")}</td><td>{b.status}</td>
              <td>{b.status === "BOOKED" && <button onClick={() => cancelBooking(b.id)}>Cancel</button>}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <h3>Daily utilization report</h3>
      <input type="date" value={repDate} onChange={(e) => setRepDate(e.target.value)} />
      <button onClick={loadReport}>Generate</button>
      <table>
        <thead><tr><th>Resource</th><th>Bookings</th></tr></thead>
        <tbody>
          {report.map((r, i) => <tr key={i}><td>{r.resource}</td><td>{r.bookings}</td></tr>)}
        </tbody>
      </table>

      <h3>Audit logs</h3>
      <input placeholder="User ID (optional)" value={logUser} onChange={(e) => setLogUser(e.target.value)} />
      <input type="date" value={logDate} onChange={(e) => setLogDate(e.target.value)} />
      <button onClick={loadLogs}>Search</button>
      <table>
        <thead><tr><th>ID</th><th>User ID</th><th>Action</th><th>Time</th></tr></thead>
        <tbody>
          {logs.map((l) => (
            <tr key={l.id}>
              <td>{l.id}</td><td>{l.userId}</td><td>{l.action}</td><td>{l.timestamp.replace("T", " ")}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
