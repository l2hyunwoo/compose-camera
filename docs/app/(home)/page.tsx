import Link from 'next/link';
import { Camera, Layers, Smartphone, Ban, ArrowRight, CheckCircle2, Terminal } from 'lucide-react';

export default function HomePage() {
  return (
    <div className="flex flex-col flex-1 w-full mx-auto justify-center overflow-x-hidden">
      {/* Hero Section */}
      <section className="relative py-24 sm:py-32">
        <div className="mx-auto max-w-7xl px-6 lg:px-8">
          <div className="lg:flex lg:items-center lg:gap-x-16">
            <div className="mx-auto max-w-2xl lg:mx-0 lg:flex-auto">
              <div className="flex">
                <div className="relative flex items-center gap-x-4 rounded-full px-4 py-1 text-sm leading-6 text-fd-muted-foreground ring-1 ring-fd-border hover:ring-fd-foreground/20 bg-fd-card/50 backdrop-blur-sm">
                  <span className="font-semibold text-fd-primary">New</span>
                  <span className="h-4 w-px bg-fd-border" aria-hidden="true" />
                  <a href="#" className="flex items-center gap-x-1 hover:text-fd-foreground">
                    v1.2.2 is now available
                    <ArrowRight className="h-3 w-3" />
                  </a>
                </div>
              </div>
              <h1 className="mt-10 text-4xl font-extrabold tracking-tight text-fd-foreground sm:text-6xl text-transparent bg-clip-text bg-gradient-to-r from-fd-foreground to-fd-foreground/60">
                Compose Camera
              </h1>
              <p className="mt-6 text-lg leading-8 text-fd-muted-foreground">
                Fascinating Camera Kit for Compose Multiplatform.
                <br />
                Built with CameraX and AVFoundation for native performance.
              </p>
              <div className="mt-10 flex items-center gap-x-6">
                <Link
                  href="/docs/quick-start"
                  className="rounded-lg bg-fd-primary px-5 py-3 text-sm font-semibold text-fd-primary-foreground shadow-sm hover:bg-fd-primary/90 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-fd-primary transition-all"
                >
                  Get Started
                </Link>
                <Link href="/docs" className="text-sm font-semibold leading-6 text-fd-foreground hover:text-fd-primary transition-colors flex items-center gap-2">
                  Read Documentation <ArrowRight className="h-4 w-4" />
                </Link>
              </div>
            </div>
            
            {/* Code Snippet Visual */}
            <div className="mt-16 sm:mt-24 lg:mt-0 lg:flex-shrink-0 lg:flex-grow">
               <div className="relative rounded-2xl bg-fd-muted/10 p-2 ring-1 ring-inset ring-fd-border/10 backdrop-blur-3xl">
                  <div className="group relative rounded-xl bg-fd-card shadow-2xl ring-1 ring-fd-border/10">
                    <div className="flex border-b border-fd-border/20 bg-fd-muted/20 px-4 py-3 items-center gap-2">
                       <div className="flex gap-1.5">
                         <div className="w-3 h-3 rounded-full bg-red-400/80" />
                         <div className="w-3 h-3 rounded-full bg-yellow-400/80" />
                         <div className="w-3 h-3 rounded-full bg-green-400/80" />
                       </div>
                       <div className="text-xs text-fd-muted-foreground font-mono ml-2">CameraScreen.kt</div>
                    </div>
                    <div className="p-6 overflow-x-auto">
                      <pre className="text-sm font-mono leading-relaxed text-fd-foreground">
                        <code>
                          <span className="text-purple-400">@Composable</span>{'\n'}
                          <span className="text-blue-400">fun</span> <span className="text-yellow-400">CameraScreen</span>() {'{'}{'\n'}
                          {'  '}<span className="text-blue-400">var</span> config <span className="text-blue-400">by</span> <span className="text-green-400">remember</span> {'{'} <span className="text-green-400">mutableStateOf</span>(<span className="text-green-400">CameraConfiguration</span>()) {'}'}{'\n'}
                          {'  '}<span className="text-blue-400">var</span> controller <span className="text-blue-400">by</span> <span className="text-green-400">remember</span> {'{'} <span className="text-green-400">mutableStateOf</span>&lt;<span className="text-green-400">CameraController</span>?&gt;(<span className="text-blue-400">null</span>) {'}'}{'\n'}
                          {'\n'}
                          {'  '}<span className="text-yellow-400">CameraPreview</span>({'\n'}
                          {'    '}modifier = Modifier.<span className="text-blue-400">fillMaxSize</span>(),{'\n'}
                          {'    '}configuration = config,{'\n'}
                          {'    '}onCameraControllerReady = {'{'} controller = it {'}'}{'\n'}
                          {'  '}){'\n'}
                          {'}'}
                        </code>
                      </pre>
                    </div>
                  </div>
               </div>
            </div>
          </div>
        </div>
        
        {/* Background Gradients */}
        <div className="absolute inset-x-0 -top-40 -z-10 transform-gpu overflow-hidden blur-3xl sm:-top-80" aria-hidden="true">
          <div className="relative left-[calc(50%-11rem)] aspect-[1155/678] w-[36.125rem] -translate-x-1/2 rotate-[30deg] bg-gradient-to-tr from-[#80caff] to-[#4f46e5] opacity-20 sm:left-[calc(50%-30rem)] sm:w-[72.1875rem]" style={{clipPath: "polygon(74.1% 44.1%, 100% 61.6%, 97.5% 26.9%, 85.5% 0.1%, 80.7% 2%, 72.5% 32.5%, 60.2% 62.4%, 52.4% 68.1%, 47.5% 58.3%, 45.2% 34.5%, 27.5% 76.7%, 0.1% 64.9%, 17.9% 100%, 27.6% 76.8%, 76.1% 97.7%, 74.1% 44.1%)"}}></div>
        </div>
      </section>

      {/* Features Grid (Bento Style) */}
      <section className="mx-auto max-w-7xl px-6 lg:px-8 pb-24">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Card 1: Cross-Platform (Large) */}
          <div className="md:col-span-2 p-8 rounded-3xl bg-fd-card border border-fd-border/50 hover:border-fd-foreground/20 transition-all shadow-sm hover:shadow-md relative overflow-hidden group">
             <div className="relative z-10">
                <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center mb-6 text-white group-hover:scale-110 transition-transform">
                  <Smartphone className="w-6 h-6" />
                </div>
                <h3 className="text-2xl font-bold mb-2">Cross-Platform Native</h3>
                <p className="text-fd-muted-foreground text-lg">
                  Unified API that leverages CameraX on Android and AVFoundation on iOS for maximum performance and reliability.
                </p>
             </div>
             <div className="absolute right-0 bottom-0 opacity-10 group-hover:opacity-20 transition-opacity">
               <Smartphone className="w-64 h-64 -mb-12 -mr-12" />
             </div>
          </div>

           {/* Card 2: Permissions */}
           <div className="p-8 rounded-3xl bg-fd-card border border-fd-border/50 hover:border-fd-foreground/20 transition-all shadow-sm hover:shadow-md">
                <div className="w-12 h-12 rounded-2xl bg-fd-secondary flex items-center justify-center mb-6 text-fd-foreground">
                  <CheckCircle2 className="w-6 h-6" />
                </div>
                <h3 className="text-xl font-bold mb-2">Unified API</h3>
                <p className="text-fd-muted-foreground">
                  A single, consistent API surface for Android and iOS. Write your camera logic once in Kotlin and run it everywhere.
                </p>
           </div>
           
           {/* Card 3: Plugins */}
           <div className="p-8 rounded-3xl bg-fd-card border border-fd-border/50 hover:border-fd-foreground/20 transition-all shadow-sm hover:shadow-md">
                <div className="w-12 h-12 rounded-2xl bg-fd-secondary flex items-center justify-center mb-6 text-fd-foreground">
                  <Layers className="w-6 h-6" />
                </div>
                <h3 className="text-xl font-bold mb-2">Extensible Architecture</h3>
                <p className="text-fd-muted-foreground">
                  Designed for scalability. Beyond simple plugins, the event-driven architecture allows deeply integrated custom features.
                </p>
           </div>
           
           {/* Card 4: Camera Preview (Large) */}
           <div className="md:col-span-2 p-8 rounded-3xl bg-fd-card border border-fd-border/50 hover:border-fd-foreground/20 transition-all shadow-sm hover:shadow-md relative overflow-hidden group">
             <div className="relative z-10">
                <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-emerald-400 to-cyan-500 flex items-center justify-center mb-6 text-white group-hover:scale-110 transition-transform">
                  <Camera className="w-6 h-6" />
                </div>
                <h3 className="text-2xl font-bold mb-2">Declarative Control</h3>
                <p className="text-fd-muted-foreground text-lg">
                  Control hardware via Compose state. Flash, zoom, and focus are managed reactively, eliminating complex view-based imperative calls.
                </p>
             </div>
              <div className="absolute right-0 bottom-0 opacity-10 group-hover:opacity-20 transition-opacity">
               <Camera className="w-64 h-64 -mb-12 -mr-12" />
             </div>
           </div>
        </div>
      </section>
    </div>
  );
}
