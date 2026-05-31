import { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, useNavigate, useParams, Navigate } from 'react-router-dom';
import './index.css';

const API_URL = 'http://localhost:8080/api';

// --- Auth Component ---
function AuthPage({ onLogin }) {
  const [activeTab, setActiveTab] = useState('login');
  const [alert, setAlert] = useState(null);
  const [formData, setFormData] = useState({ name: '', email: '', password: '', confirmPassword: '' });

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    const isLogin = activeTab === 'login';
    if (!isLogin && formData.password !== formData.confirmPassword) return setAlert({ type: 'error', message: 'Passwords do not match!' });

    try {
      const endpoint = isLogin ? '/auth/login' : '/auth/register';
      const payload = isLogin ? { email: formData.email, password: formData.password } : { name: formData.name, email: formData.email, password: formData.password };
      const res = await fetch(`${API_URL}${endpoint}`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
      const data = await res.json();

      if (data.success) {
        if (isLogin) {
          localStorage.setItem('userEmail', formData.email);
          onLogin(formData.email);
        } else {
          setAlert({ type: 'success', message: 'Account created! Please log in.' });
          setActiveTab('login');
        }
      } else {
        setAlert({ type: 'error', message: data.message });
      }
    } catch (e) {
      setAlert({ type: 'error', message: 'Connection error.' });
    }
  };

  return (
    <div className="glass-panel fade-in" style={{ maxWidth: '480px' }}>
      <h1 className="title">Dream Vacation</h1>
      <p className="subtitle">Plan your perfect getaway</p>
      
      <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
        <button className={`btn ${activeTab === 'login' ? '' : 'btn-secondary'}`} onClick={() => setActiveTab('login')}>LOG IN</button>
        <button className={`btn ${activeTab === 'register' ? '' : 'btn-secondary'}`} onClick={() => setActiveTab('register')}>SIGN UP</button>
      </div>
      
      {alert && <div className={`alert ${alert.type}`}>{alert.message}</div>}

      <form onSubmit={handleSubmit}>
        {activeTab === 'register' && (
          <div className="form-group">
            <label>Full Name</label>
            <input type="text" name="name" className="input-field" onChange={handleChange} required />
          </div>
        )}
        <div className="form-group">
          <label>Email</label>
          <input type="email" name="email" className="input-field" onChange={handleChange} required />
        </div>
        <div className="form-group">
          <label>Password</label>
          <input type="password" name="password" className="input-field" onChange={handleChange} required />
        </div>
        {activeTab === 'register' && (
          <div className="form-group">
            <label>Confirm Password</label>
            <input type="password" name="confirmPassword" className="input-field" onChange={handleChange} required />
          </div>
        )}
        <button type="submit" className="btn">{activeTab === 'login' ? 'LOG IN' : 'CREATE ACCOUNT'}</button>
      </form>
    </div>
  );
}

// --- Dashboard Component ---
function Dashboard() {
  const navigate = useNavigate();
  const email = localStorage.getItem('userEmail');
  const [vacations, setVacations] = useState([]);

  useEffect(() => {
    fetch(`${API_URL}/vacations/user/${email}`)
      .then(res => res.json())
      .then(data => setVacations(data || []));
  }, [email]);

  const handleDelete = async (e, id) => {
    e.stopPropagation(); // Prevent triggering the card's onClick
    if (!window.confirm("Are you sure you want to delete this vacation?")) return;
    
    await fetch(`${API_URL}/vacations/${id}`, { method: 'DELETE' });
    setVacations(vacations.filter(v => v.id !== id));
  };

  return (
    <div className="glass-panel fade-in">
      <div className="top-bar">
        <h2 className="title" style={{margin:0}}>My Vacations</h2>
        <button className="btn-logout" onClick={() => { localStorage.clear(); window.location.reload(); }}>Logout</button>
      </div>
      
      <button className="btn" onClick={() => navigate('/preferences')}>+ Create New Vacation</button>
      
      <div className="card-grid">
        {vacations.map(v => (
          <div className="card" key={v.id} onClick={() => navigate(v.status === 'Fully Paid' || v.status.includes('Reserved') ? `/checkout/${v.id}` : `/activities/${v.id}`)} style={{position: 'relative'}}>
            <div className="card-title">{v.destination || v.type + ' Trip'}</div>
            <div className="card-desc">{v.startDate} to {v.endDate}</div>
            <div className="card-price" style={{fontSize: '14px', color: '#666'}}>{v.status}</div>
            <button 
              onClick={(e) => handleDelete(e, v.id)} 
              style={{position: 'absolute', top: '10px', right: '10px', background: 'transparent', border: 'none', color: '#ff4757', fontSize: '20px', cursor: 'pointer', padding: '5px'}}
              title="Delete Vacation"
            >
              🗑️
            </button>
          </div>
        ))}
        {vacations.length === 0 && <p style={{marginTop: '20px'}}>No vacations planned yet.</p>}
      </div>
    </div>
  );
}

// --- Custom Escape Planner (Side-by-side) ---
function Planner() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ type: 'Sea', startDate: '', endDate: '', numberOfPeople: '', budget: '' });
  const [packages, setPackages] = useState([]);
  const [selectedPkg, setSelectedPkg] = useState(null);
  
  const handleReview = async (e) => {
    e.preventDefault();
    const res = await fetch(`${API_URL}/packages?type=${formData.type}`);
    const data = await res.json();
    setPackages(data);
  };

  const handleSave = async () => {
    if (!selectedPkg) return alert("Please select a package from the right panel!");
    const payload = { ...formData, email: localStorage.getItem('userEmail') };
    const res = await fetch(`${API_URL}/vacations`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
    const data = await res.json();
    if (data.success) {
      await fetch(`${API_URL}/vacations/${data.vacation.id}/package`, { method: 'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({packageId: selectedPkg})});
      navigate(`/activities/${data.vacation.id}`);
    } else {
      alert("Error saving vacation");
    }
  };

  return (
    <div className="planner-container fade-in">
      <div className="planner-panel">
        <h2 className="panel-header">VACATION DETAILS</h2>
        <form onSubmit={handleReview} style={{display:'flex', flexDirection:'column', flex:1}}>
          <div className="form-group">
            <label>VACATION TYPE</label>
            <select name="type" className="input-field" onChange={e => setFormData({...formData, type: e.target.value})}>
              <option value="Sea">Sea</option>
              <option value="Mountain">Mountain</option>
              <option value="Business">Business</option>
              <option value="Romantic">Romantic</option>
            </select>
          </div>
          <div className="form-group">
            <label>START DATE</label>
            <input type="date" name="startDate" className="input-field" onChange={e => setFormData({...formData, startDate: e.target.value})} required />
          </div>
          <div className="form-group">
            <label>END DATE</label>
            <input type="date" name="endDate" className="input-field" onChange={e => setFormData({...formData, endDate: e.target.value})} required />
          </div>
          <div className="form-group">
            <label>NUMBER OF PEOPLE</label>
            <input type="number" name="numberOfPeople" className="input-field" placeholder="Persons: (e.g., 2)" onChange={e => setFormData({...formData, numberOfPeople: e.target.value})} required min="1" />
          </div>
          <div className="form-group">
            <label>BUDGET ($)</label>
            <input type="number" name="budget" className="input-field" placeholder="e.g., $1000" onChange={e => setFormData({...formData, budget: e.target.value})} required />
          </div>
          <div style={{flex:1}}></div>
          <button type="submit" className="btn" style={{background: 'rgba(45,41,66,0.9)'}}>REVIEW PREFERENCES</button>
        </form>
      </div>
      
      <div className="planner-panel">
        <h2 className="panel-header">SELECT PACKAGES</h2>
        <div style={{flex:1, overflowY: 'auto', paddingRight: '10px'}}>
          {packages.length === 0 ? (
            <p style={{textAlign:'center', color:'#666', marginTop:'50px'}}>Fill details on the left and click Review to see packages.</p>
          ) : (
            packages.map(p => (
              <div key={p.id} className={`package-item ${selectedPkg === p.id ? 'selected' : ''}`} onClick={() => setSelectedPkg(p.id)}>
                <div className="package-header">
                  <span className="package-title">{p.bundleName} - {p.destination}</span>
                  <div className="package-checkbox"></div>
                </div>
                <div className="package-image-container">
                  <img src={p.imageUrl || "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"} alt="Destination" className="package-img" />
                  <div className="package-info">
                    <div className="package-cost">PRICE: {p.price} € / person</div>
                    <div className="package-description">{p.description}</div>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
        <button className="btn" onClick={handleSave} disabled={!selectedPkg} style={{background: 'rgba(45,41,66,0.9)'}}>SAVE VACATION</button>
      </div>
    </div>
  );
}

// --- Manage Activities (UC-5) ---
function Activities() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [suggestions, setSuggestions] = useState([]);
  const [vacation, setVacation] = useState(null);
  
  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [custom, setCustom] = useState({ title: '', description: '', price: '', imageUrl: '🧘‍♂️' });

  const ICONS = ['🧘‍♂️', '🌴', '🚲', '☕', '🛥️'];

  useEffect(() => {
    fetch(`${API_URL}/vacations/${id}`).then(res => res.json()).then(data => {
      setVacation(data);
      fetch(`${API_URL}/activities/suggestions?type=${data.type}`).then(r => r.json()).then(s => setSuggestions(s));
    });
  }, [id]);

  const addActivity = async (activity, isCustom = false) => {
    let payload = activity;
    if (isCustom) {
      if (!custom.title || !custom.price) return alert("Please fill title and price");
      payload = {
        title: custom.title,
        description: custom.description,
        price: parseFloat(custom.price),
        imageUrl: custom.imageUrl // Can be emoji or URL
      };
    }
    await fetch(`${API_URL}/vacations/${id}/activities`, { method: 'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload) });
    if (isCustom) {
      setShowModal(false);
      setCustom({ title: '', description: '', price: '', imageUrl: '🧘‍♂️' });
    }
    // Remove the suggestion if it was a suggested activity so it acts like a toggle (or just alert)
    if (!isCustom) {
      setSuggestions(suggestions.filter(s => s.id !== activity.id));
    }
  };

  return (
    <div className="glass-panel fade-in" style={{maxWidth: '600px'}}>
      <h2 className="title">MANAGE ACTIVITIES</h2>
      
      <div style={{marginTop: '20px', marginBottom: '20px'}}>
        {suggestions.map(s => (
          <div key={s.id} className="package-item" style={{marginBottom: '15px'}} onClick={() => addActivity(s, false)}>
            <div className="package-header">
              <span className="package-title" style={{textTransform:'uppercase'}}>{s.title}</span>
              <div className="package-checkbox"></div>
            </div>
            <div className="package-image-container">
              <img src={s.imageUrl || "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"} alt="Activity" className="package-img" style={{width:'80px', height:'80px'}} />
              <div className="package-info">
                <div className="package-description">{s.description}</div>
                <div className="package-cost" style={{color:'#666', marginTop:'5px'}}>Price e.g., ${s.price}</div>
              </div>
            </div>
          </div>
        ))}
      </div>

      <button className="dashed-btn" onClick={() => setShowModal(true)}>
        <span style={{fontSize:'24px', marginRight:'5px'}}>+</span> ADD CUSTOM ACTIVITY
      </button>

      <button className="btn" style={{background: 'rgba(45,41,66,0.9)'}} onClick={() => navigate(`/checkout/${id}`)}>REVIEW PREFERENCES</button>

      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content fade-in">
            <h3 style={{marginBottom:'20px', color:'var(--primary)'}}>ADD NEW CUSTOM ACTIVITY</h3>
            
            <div className="form-group">
              <label>Activity Title / Name</label>
              <input type="text" className="input-field" placeholder="e.g., Sunset Yoga" value={custom.title} onChange={e => setCustom({...custom, title: e.target.value})} />
            </div>
            
            <div className="form-group">
              <label>Description</label>
              <textarea className="input-field" placeholder="Description (e.g., Evening yoga session by the shore)" rows="3" value={custom.description} onChange={e => setCustom({...custom, description: e.target.value})}></textarea>
            </div>
            
            <div className="form-group">
              <label>Estimated Price ($)</label>
              <input type="number" className="input-field" placeholder="$" value={custom.price} onChange={e => setCustom({...custom, price: e.target.value})} />
            </div>

            <div className="form-group">
              <label>Select Icon</label>
              <div className="icon-selector">
                {ICONS.map(icon => (
                  <button key={icon} className={`icon-btn ${custom.imageUrl === icon ? 'selected' : ''}`} onClick={() => setCustom({...custom, imageUrl: icon})}>
                    {icon}
                  </button>
                ))}
              </div>
            </div>

            <div style={{display:'flex', gap:'10px', marginTop:'20px'}}>
              <button className="btn" style={{flex:1, margin:0, background: 'rgba(45,41,66,0.9)'}} onClick={() => addActivity(null, true)}>Add Activity</button>
              <button className="btn btn-secondary" style={{flex:1, margin:0}} onClick={() => setShowModal(false)}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// --- Checkout / Pay Vacation (UC-6) ---
function Checkout() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [confirmOverride, setConfirmOverride] = useState(false);

  useEffect(() => {
    fetch(`${API_URL}/vacations/${id}/summary`).then(res => res.json()).then(data => setSummary(data));
  }, [id]);

  const handlePay = async () => {
    if (summary.totalCost > summary.vacation.budget && !confirmOverride) {
      setConfirmOverride(true);
      return;
    }

    const res = await fetch(`${API_URL}/vacations/${id}/pay?force=${confirmOverride}`, { method: 'POST' });
    const data = await res.json();
    if (data.success) {
      setSuccess(true);
      setError(null);
      setTimeout(() => navigate('/dashboard'), 2000);
    } else {
      setError(data.message);
    }
  };

  if (!summary) return <div>Loading...</div>;

  return (
    <div className="glass-panel fade-in">
      <h2 className="title">Checkout</h2>
      <p className="subtitle">Review and pay for your vacation</p>

      {error && <div className="alert error">{error}</div>}
      {success && <div className="alert success">Payment Successful! Redirecting...</div>}
      {confirmOverride && !success && (
        <div className="alert error" style={{ fontWeight: 'bold' }}>
          Atentie! Ai depasit bugetul alocat cu ${(summary.totalCost - summary.vacation.budget).toFixed(2)}. Esti sigur ca vrei sa platesti?
        </div>
      )}

      <div style={{background: 'rgba(255,255,255,0.5)', padding: '20px', borderRadius: '16px', marginBottom: '20px'}}>
        <h3>{summary.vacation.destination} - {summary.vacation.selectedPackage?.bundleName}</h3>
        <p>Package Price: ${summary.vacation.selectedPackage?.price}</p>
        
        <h4 style={{marginTop: '15px'}}>Activities:</h4>
        <ul style={{marginLeft: '20px', color: '#666'}}>
          {summary.activities.map(a => <li key={a.id}>{a.title}: ${a.price}</li>)}
        </ul>

        <hr style={{margin: '20px 0', borderColor: 'rgba(0,0,0,0.1)'}}/>
        <div style={{display: 'flex', justifyContent: 'space-between', fontSize: '20px', fontWeight: 'bold'}}>
          <span>Total Cost (+10% agency fee):</span>
          <span style={{color: '#2ed573'}}>${summary.totalCost.toFixed(2)}</span>
        </div>
        <div style={{display: 'flex', justifyContent: 'space-between', fontSize: '16px', marginTop: '10px', color: '#666'}}>
          <span>Your Budget:</span>
          <span>${summary.vacation.budget}</span>
        </div>
      </div>

      {summary.vacation.status === 'Fully Paid' ? (
        <div className="alert success" style={{ fontWeight: 'bold', textAlign: 'center' }}>
          Vacation is Fully Paid! Have a great trip! ✈️
        </div>
      ) : (
        <button className="btn" onClick={handlePay} disabled={success}>
          {confirmOverride ? "Da, Sunt Sigur (Plateste)" : "Confirm & Pay"}
        </button>
      )}
      <button className="btn btn-secondary" onClick={() => navigate('/dashboard')} style={{marginTop: '10px'}}>Back to Dashboard</button>
    </div>
  );
}


// --- Main App Router ---
export default function App() {
  const [userEmail, setUserEmail] = useState(localStorage.getItem('userEmail'));

  if (!userEmail) {
    return <AuthPage onLogin={(email) => setUserEmail(email)} />;
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/preferences" element={<Planner />} />
        <Route path="/activities/:id" element={<Activities />} />
        <Route path="/checkout/:id" element={<Checkout />} />
      </Routes>
    </BrowserRouter>
  );
}
