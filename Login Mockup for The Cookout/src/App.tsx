import { useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './components/ui/card'
import { Input } from './components/ui/input'
import { Label } from './components/ui/label'
import { Button } from './components/ui/button'
import { Separator } from './components/ui/separator'
import { ImageWithFallback } from './components/figma/ImageWithFallback'
import { Eye, EyeOff } from 'lucide-react'
import cookoutLogo from 'figma:asset/69e464e1b7c477d071aada0d283f43f9b433810a.png'
import backdropImage from 'figma:asset/2aa676a44fa5345b46b1c17f416643c4e04873d6.png'

export default function App() {
  const [showPassword, setShowPassword] = useState(false)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  return (
    <div className="min-h-screen bg-gradient-to-b from-orange-400 via-orange-300 to-orange-50">
      {/* Header with hero background */}
      <div className="relative h-72 overflow-hidden">
        <img
          src={backdropImage}
          alt="People cooking together and sharing recipes"
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-b from-orange-900/30 via-orange-800/20 to-orange-600/20" />
        
        {/* App branding */}
        <div className="absolute top-16 left-0 right-0 text-center">
          <div className="flex items-center justify-center gap-3 mb-4">
            <div className="w-16 h-16 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center border border-white/30 p-2">
              <img 
                src={cookoutLogo} 
                alt="The Cookout Logo" 
                className="w-full h-full object-contain"
              />
            </div>
          </div>
          <h1 className="text-3xl text-white mb-2">The Cookout</h1>
          <p className="text-orange-100 text-lg">Share your recipes!</p>
        </div>
      </div>

      {/* Login form container */}
      <div className="flex-1 px-6 -mt-8 relative z-10">
        <Card className="shadow-2xl border-0 bg-white rounded-t-3xl">
          <CardHeader className="text-center pt-8 pb-6">
            <CardTitle className="text-gray-800 text-xl">Welcome back!</CardTitle>
            <CardDescription className="text-base">
              Sign in to your account to continue cooking
            </CardDescription>
          </CardHeader>
          
          <CardContent className="px-6 pb-8 space-y-6">
            <div className="space-y-5">
              <div className="space-y-2">
                <Label htmlFor="email" className="text-gray-700">Email</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="chef@cookout.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="h-12 border-gray-200 focus:border-orange-400 focus:ring-orange-400/20 bg-gray-50 rounded-xl"
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="password" className="text-gray-700">Password</Label>
                <div className="relative">
                  <Input
                    id="password"
                    type={showPassword ? "text" : "password"}
                    placeholder="Enter your password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="h-12 border-gray-200 focus:border-orange-400 focus:ring-orange-400/20 bg-gray-50 rounded-xl pr-12"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 p-1"
                  >
                    {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
              </div>
            </div>

            <div className="flex items-center justify-between pt-2">
              <label className="flex items-center space-x-3">
                <input
                  type="checkbox"
                  className="w-4 h-4 rounded border-gray-300 text-orange-500 focus:ring-orange-500 focus:ring-2"
                />
                <span className="text-gray-600">Remember me</span>
              </label>
              <button className="text-orange-600 hover:text-orange-700 hover:underline">
                Forgot password?
              </button>
            </div>

            <div className="space-y-4 pt-2">
              <Button 
                className="w-full h-12 bg-orange-500 hover:bg-orange-600 text-white shadow-lg rounded-xl"
                size="lg"
              >
                Sign In
              </Button>

              <div className="relative">
                <Separator className="bg-gray-200" />
                <span className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 bg-white px-4 text-gray-500">
                  or
                </span>
              </div>

              <Button 
                variant="outline" 
                className="w-full h-12 border-gray-200 hover:bg-gray-50 rounded-xl"
                size="lg"
              >
                <svg className="w-5 h-5 mr-3" viewBox="0 0 24 24">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
                Continue with Google
              </Button>
            </div>

            <div className="text-center pt-4">
              <span className="text-gray-600">
                Don't have an account?{' '}
                <button className="text-orange-600 hover:text-orange-700 hover:underline">
                  Sign up
                </button>
              </span>
            </div>
          </CardContent>
        </Card>

        {/* Footer */}
        <div className="py-6 text-center">
          <p className="text-gray-500 text-sm">© 2025 The Cookout. All rights reserved.</p>
        </div>
      </div>
    </div>
  )
}